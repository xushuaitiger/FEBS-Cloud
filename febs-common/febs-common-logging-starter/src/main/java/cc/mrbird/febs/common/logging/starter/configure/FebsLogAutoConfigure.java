package cc.mrbird.febs.common.logging.starter.configure;

import cc.mrbird.febs.common.logging.starter.properties.FebsLogProperties;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.logstash.logback.appender.LogstashTcpSocketAppender;
import net.logstash.logback.encoder.LogstashEncoder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * @author: xuefrye
 */
@Configuration
@EnableConfigurationProperties(FebsLogProperties.class)
public class FebsLogAutoConfigure {
    private final FebsLogProperties properties;

    @Value("${spring.application.name}")
    private String applicationName;

    public FebsLogAutoConfigure(FebsLogProperties properties) {
        this.properties = properties;
    }

    @ConditionalOnProperty(name = "febs.log.enable-elk", havingValue = "true", matchIfMissing = true)
    @Bean
    public void enableElk() throws JsonProcessingException {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger("ROOT");
        LogstashTcpSocketAppender appender = new LogstashTcpSocketAppender();
        LogstashEncoder encoder = new LogstashEncoder();

        HashMap<String, String> customFields = new HashMap<>();
        customFields.put("application-name",applicationName);
        String customFieldsString = new ObjectMapper().writeValueAsString(customFields);
        encoder.setCustomFields(customFieldsString);

        appender.setEncoder(encoder);
        appender.addDestination(properties.getLogstashHost());
        appender.setName("logstash[" + applicationName + "]");
        appender.start();

        logger.addAppender(appender);
    }
}
