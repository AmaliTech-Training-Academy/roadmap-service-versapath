package com.capstone.messaging;

import com.capstone.exception.SkillAtomProcessingException;
import com.capstone.model.SkillAtomSnapshot;
import com.capstone.service.SkillAtomSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.SkillAtomEvent;
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
public class AtomKafkaConsumer {
    private final SkillAtomSnapshotService skillAtomSnapshotService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${KAFKA_ATOM_DLT_TOPIC:atom.create.dlt}")
    private String atomDltTopic;

    @KafkaListener(topics = "${KAFKA_ATOM_TOPIC:atom.create}")
    @Retryable(
            retryFor = {SkillAtomProcessingException.class, Exception.class},
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void listenAtomCreate(
            @Payload SkillAtomEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received atom.create event from topic: {}, partition: {}, offset: {}, atomId: {}",
                topic, partition, offset, event.getId());

        try {
            // Validate event
            validateSkillAtomEvent(event);

            // Process the skill atom event
            SkillAtomSnapshot processedAtom = skillAtomSnapshotService.processSkillAtomEvent(event);

            log.info("Successfully processed skill atom event for atomId: {}, internal ID: {}",
                    event.getId(), processedAtom.getId());

            // Acknowledge message only after successful processing
            acknowledgment.acknowledge();

        } catch (SkillAtomProcessingException e) {
            log.error("Failed to process skill atom event for atomId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment);

        } catch (Exception e) {
            log.error("Unexpected error processing skill atom event for atomId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment);
        }
    }

    private void validateSkillAtomEvent(SkillAtomEvent event) {
        log.debug("Validating skill atom event: {}", event);

        if (event == null) {
            throw new SkillAtomProcessingException("Skill atom event cannot be null");
        }

        if (event.getId() == null) {
            throw new SkillAtomProcessingException("Skill atom event must contain a valid ID");
        }

        if (event.getName() == null || event.getName().trim().isEmpty()) {
            throw new SkillAtomProcessingException("Skill atom event must contain a valid name");
        }

        log.debug("Skill atom event validation successful for atomId: {}", event.getId());
    }

    private void handleProcessingFailure(SkillAtomEvent event, Acknowledgment acknowledgment) {
        String atomId = event.getId().toString();

        log.error("Processing failed for skill atom event with atomId: {}. Sending to DLT topic: {}",
                atomId, atomDltTopic);

        try {
            // Send to Dead Letter Topic for manual review/reprocessing
            kafkaTemplate.send(atomDltTopic, atomId, event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent failed skill atom event to DLT for atomId: {}", atomId);
                        } else {
                            log.error("Failed to send skill atom event to DLT for atomId: {}", atomId, ex);
                        }
                    });

            // Acknowledge the original message to prevent infinite retries
            acknowledgment.acknowledge();

        } catch (Exception dltException) {
            log.error("Critical: Failed to send skill atom message to DLT for atomId: {}. Message will be retried by Kafka",
                    atomId, dltException);
        }
    }
}
