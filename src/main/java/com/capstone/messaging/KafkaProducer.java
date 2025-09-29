package com.capstone.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.LearnerOnBoardingEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${KAFKA_LEARNER_ONBOARD_TOPIC:learner.onboard}")
    private String learnerOnboardTopic;

    public void produce(LearnerOnBoardingEvent event) {
        log.info("Sending event to Kafka topic: {}", "learner.onboard");
        kafkaTemplate.send(learnerOnboardTopic, event);
        log.info("Learner Onboard event is populated: {}", event);
    }
}
