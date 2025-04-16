package com.transformer.mq.consumer;

import com.zto.consumer.MsgConsumedStatus;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka消息消费者
 *
 * @author ouliyuan 2023/7/18
 */
@Slf4j
public abstract class KafkaMessageConsumer<T> extends MessageConsumer<T> {

    /**
     * 消费Kafka消息
     *
     * @param group
     * @param body    消息体
     * @param queueId partition id
     * @param offset  消息偏移量
     * @return
     */
    public MsgConsumedStatus consumer(String group, String body, String queueId, String offset) {
        MsgConsumedStatus status;
        try {
            long start = System.currentTimeMillis();
            // 处理消息
            boolean success = handler(body);

            // 记录处理日志
            logger(group, body, queueId, offset, success, start);

            status = success ? MsgConsumedStatus.SUCCEED : MsgConsumedStatus.RETRY;
        } catch (IllegalArgumentException e) {
            // 记录异常日志
            error(group, body, queueId, offset, "illegal argument", e);

            status = MsgConsumedStatus.SUCCEED;
        } catch (Exception e) {
            // 记录异常日志
            error(group, body, queueId, offset, "exception", e);

            status = MsgConsumedStatus.RETRY;
        }

        // 不成功时，尝试再次投递
        if (status != MsgConsumedStatus.SUCCEED) {
            return retry(status, body);
        }

        // 成功消费返回成功
        return status;
    }

    private void logger(String group, String body, String queueId, String offset, boolean success, long startTime) {
        log.warn("consume {}, cost:{}ms, group:{}, queueId:{}, offset:{}, body:{}", success ? "success" : "failure", System.currentTimeMillis() - startTime, group, queueId, offset, getMessageBody(body));
    }

    private void error(String group, String body, String queueId, String offset, String message, Exception e) {
        log.error("consume {}, group:{}, queueId:{}, offset:{}, body:{}", message, group, queueId, offset, getMessageBody(body), e);
    }

    protected String getMessageBody(String body) {
        return body;
    }

    protected MsgConsumedStatus retry(MsgConsumedStatus status, String body) {
        return status;
    }
}
