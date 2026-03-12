package ru.sg.paymentservice.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServer;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic paymentEventTopic() {
        return new NewTopic("payment-events", 3, (short) 1).configs(Map.of("retention.ms", "604800000", "compression.type", "snappy"));
    }

    @Bean
    public NewTopic orderEventTopic() {
        return new NewTopic("order-events", 3, (short) 1).configs(Map.of("retention.ms", "604800000"));
    }

    @Bean
    public NewTopic deadLetterTopic() {
        return new NewTopic("payment-events-dlq", 1, (short) 1).configs(Map.of("retention.ms", "2592000000", "compression.type", "snappy"));
    }
}
