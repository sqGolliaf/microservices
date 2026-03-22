package ru.sg.email.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import ru.sg.email.dto.event.UserRegisteredEvent

@EnableKafka
@Configuration
class KafkaConfig {

    @Bean
    fun consumerFactory(): ConsumerFactory<String, UserRegisteredEvent> {
        val configProps = mutableMapOf<String, Any>()
        configProps[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = ""
        configProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        configProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = UserRegisteredEvent::class.java
        configProps[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false

        return DefaultKafkaConsumerFactory(configProps)
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, UserRegisteredEvent> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, UserRegisteredEvent>()
        factory.setContainerCustomizer { container ->
            container.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
        }

        return factory
    }
}