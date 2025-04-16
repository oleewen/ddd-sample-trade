package com.kylin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationPidFileWriter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 应用启动器
 *
 * @author only
 * @since 2020-05-22
 */
@SpringBootApplication
public class ApplicationStarter extends WebMvcConfigurerAdapter {
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        ObjectMapper objectMapper = new ObjectMapper();
        //蛇皮走位
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        //忽略无法识别字段
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //忽略空值
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        //long以String表达
        /*SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(simpleModule);*/
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
        converters.add(converter);
    }

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ApplicationStarter.class);
        File pidFile = new File("app.pid");
        pidFile.setWritable(true, true);
        pidFile.setExecutable(false);
        pidFile.setReadable(true);
        application.addListeners(new ApplicationPidFileWriter(pidFile));
        application.run(args);
    }
}
