package ru.sg.order.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationListener {

//    @KafkaListener(
//            topics = "user-events",
//            groupId = "order-service-group",
//            containerFactory = "kafkaListenerContainerFactory"
//    )
//    public void handlerUserRegistration(UserRegisteredEvent event) {
//        log.info("New user registered: {}", event.getUserName());
//    }
}
