package com.capstone.messaging;

import com.capstone.exception.UserProcessingException;
import com.capstone.model.UserSnapshot;
import com.capstone.service.UserSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.ProduceUserEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

    private final UserSnapshotService userSnapshotService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.dlt-topic:user.create.dlt}")
    private String dltTopic;

    @KafkaListener(topics = "user.create")
    @Retryable(
            retryFor = {UserProcessingException.class, Exception.class},
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void listen(
            @Payload ProduceUserEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received user.create event from topic: {}, partition: {}, offset: {}, userId: {}",
                topic, partition, offset, event.getVersapathUserId());

        try {
            // Validate event
            validateUserEvent(event);

            // Process the user event
            UserSnapshot processedUser = userSnapshotService.processUserEvent(event);

            log.info("Successfully processed user event for userId: {}, internal ID: {}",
                    event.getVersapathUserId(), processedUser.getId());

            // Acknowledge message only after successful processing
            acknowledgment.acknowledge();

        } catch (UserProcessingException e) {
            log.error("Failed to process user event for userId: {}. Error: {}",
                    event.getVersapathUserId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment);

        } catch (Exception e) {
            log.error("Unexpected error processing user event for userId: {}. Error: {}",
                    event.getVersapathUserId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment);
        }
    }

    private void validateUserEvent(ProduceUserEvent event) {
        log.debug("Validating user event: {}", event);

        if (event == null) {
            throw new UserProcessingException("User event cannot be null");
        }

        if (event.getVersapathUserId() == null) {
            throw new UserProcessingException("User event must contain a valid userId");
        }

        if (event.getEmail() == null || event.getEmail().trim().isEmpty()) {
            throw new UserProcessingException("User event must contain a valid email");
        }

        if (event.getFirstName() == null || event.getFirstName().trim().isEmpty()) {
            throw new UserProcessingException("User event must contain a valid first name");
        }

        if (event.getLastName() == null || event.getLastName().trim().isEmpty()) {
            throw new UserProcessingException("User event must contain a valid last name");
        }

        if (event.getUsername() == null || event.getUsername().trim().isEmpty()) {
            throw new UserProcessingException("User event must contain a valid username");
        }

        log.debug("User event validation successful for userId: {}", event.getVersapathUserId());
    }

    private void handleProcessingFailure(ProduceUserEvent event, Acknowledgment acknowledgment) {
        String userId = event.getVersapathUserId().toString();

        log.error("Processing failed for user event with userId: {}. Sending to DLT topic: {}",
                userId, dltTopic);

        try {
            // Send to Dead Letter Topic for manual review/reprocessing
            kafkaTemplate.send(dltTopic, userId, event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent failed event to DLT for userId: {}", userId);
                        } else {
                            log.error("Failed to send event to DLT for userId: {}", userId, ex);
                        }
                    });

            // Acknowledge the original message to prevent infinite retries
            acknowledgment.acknowledge();

        } catch (Exception dltException) {
            log.error("Critical: Failed to send message to DLT for userId: {}. Message will be retried by Kafka",
                    userId, dltException);
        }
    }
}
