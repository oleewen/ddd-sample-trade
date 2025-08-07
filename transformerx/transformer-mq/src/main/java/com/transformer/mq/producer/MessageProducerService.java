package com.transformer.mq.producer;

import com.zto.producer.SendResponse;
import com.zto.producer.ZmsCallBack;
import com.zto.titans.mq.configuration.ZMSTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class MessageProducerService {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Resource
    private ZMSTemplate zmsTemplate;

    public String send(String topic, String message) {
        log.warn("send topic:{}, message:{}", topic, message);
        if (StringUtils.isNotBlank(topic) && StringUtils.isNotBlank(message)) {
            try {
                return zmsTemplate.send(topic, message);
            } catch (Exception e) {
                // 出现异常，再尝试投递一次
                return zmsTemplate.send(topic, message);
            }
        }
        return null;
    }

    public String async(String topic, String message) {
        log.warn("send topic:{}, message:{}", topic, message);
        if (StringUtils.isNotBlank(topic) && StringUtils.isNotBlank(message)) {
            zmsTemplate.asyncSend(
                    topic, //消费队列
                    message, //消息内容
                    new ZmsCallBack() { //发送结果回调
                        @Override
                        public void onResult(SendResponse response) {
                            // 再尝试发送一次
                            zmsTemplate.send(topic, message);
                            log.error("send topic:{}, msgId:{} success", topic, response.getMsgId());
                        }
                        @Override
                        public void onException(Throwable e) {
                            log.error("send topic:{}, message:{} exception", topic, message, e);
                        }
                    });
        }
        return null;
    }
}