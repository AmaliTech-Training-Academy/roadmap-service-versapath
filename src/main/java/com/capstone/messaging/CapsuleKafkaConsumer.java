package com.capstone.messaging;

import com.capstone.exception.CapsuleAtomMappingException;
import com.capstone.exception.SkillAtomNotFoundException;
import com.capstone.exception.SkillCapsuleNotFoundException;
import com.capstone.exception.SkillCapsuleProcessingException;
import com.capstone.model.SkillCapsuleSnapshot;
import com.capstone.service.SkillCapsuleSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.SkillCapsuleEvent;
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

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CapsuleKafkaConsumer {
    private final SkillCapsuleSnapshotService skillCapsuleSnapshotService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${KAFKA_CAPSULE_DLT_TOPIC:capsule.create.dlt}")
    private String capsuleDltTopic;

    @KafkaListener(topics = "${KAFKA_CAPSULE_TOPIC:capsule.create}")
    @Retryable(
            retryFor = {SkillCapsuleProcessingException.class, CapsuleAtomMappingException.class, Exception.class},
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void listenCapsuleCreate(
            @Payload SkillCapsuleEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received capsule.create event from topic: {}, partition: {}, offset: {}, capsuleId: {}",
                topic, partition, offset, event.getId());

        try {

            validateSkillCapsuleEvent(event);

            // Process the skill capsule event
            SkillCapsuleSnapshot processedCapsule = skillCapsuleSnapshotService.processSkillCapsuleEvent(event);

            log.info("Successfully processed skill capsule event for capsuleId: {}, internal ID: {}, atoms: {}",
                    event.getId(), processedCapsule.getId(),
                    event.getSkillAtom() != null ? event.getSkillAtom().size() : 0);

            // Acknowledge message only after successful processing
            acknowledgment.acknowledge();

        } catch (SkillCapsuleProcessingException e) {
            log.error("Failed to process skill capsule event for capsuleId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment,"CREATE");

        } catch (CapsuleAtomMappingException e) {
            log.error("Failed to process capsule-atom mappings for capsuleId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment,"CREATE");

        } catch (SkillAtomNotFoundException e) {
            log.error("Missing skill atom reference for capsuleId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment,"CREATE");

        } catch (Exception e) {
            log.error("Unexpected error processing skill capsule event for capsuleId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment,"CREATE");
        }
    }

    /**
     * Comprehensive validation of SkillCapsuleEvent
     */
    private void validateSkillCapsuleEvent(SkillCapsuleEvent event) {
        log.debug("Validating skill capsule event: {}", event);

        // Basic event validation
        if (event == null) {
            throw new SkillCapsuleProcessingException("Skill capsule event cannot be null");
        }

        if (event.getId() == null) {
            throw new SkillCapsuleProcessingException("Skill capsule event must contain a valid ID");
        }

        if (event.getName() == null || event.getName().trim().isEmpty()) {
            throw new SkillCapsuleProcessingException("Skill capsule event must contain a valid name");
        }

        // Optional field validations
        if (event.getMoodleCourseId() < 0) {
            throw new SkillCapsuleProcessingException("Moodle course ID must be non-negative if provided");
        }

        if (event.getDifficulty() != null && event.getDifficulty().trim().isEmpty()) {
            throw new SkillCapsuleProcessingException("Difficulty level cannot be empty if provided");
        }

        if (event.getProficiencyLevel() != null && event.getProficiencyLevel().trim().isEmpty()) {
            throw new SkillCapsuleProcessingException("Proficiency level cannot be empty if provided");
        }

        // Validate skillAtom list structure - O(m) where m = number of atom mappings
        if (event.getSkillAtom() != null) {
            validateSkillAtomMappings(event.getSkillAtom(), event.getId());
        }

        log.debug("Skill capsule event validation successful for capsuleId: {}", event.getId());
    }

    /**
     * Validate skillAtom mapping structure and business rules
     */
    private void validateSkillAtomMappings(List<Map<UUID, Integer>> skillAtomMappings, UUID capsuleId) {
        if (skillAtomMappings.isEmpty()) {
            log.warn("Skill capsule {} has empty skillAtom list - no learning path defined", capsuleId);
            return;
        }

        Set<UUID> atomIds = new HashSet<>();
        Set<Integer> sequences = new HashSet<>();

        for (Map<UUID, Integer> atomMap : skillAtomMappings) {
            if (atomMap == null || atomMap.isEmpty()) {
                throw new SkillCapsuleProcessingException("Skill atom mapping cannot be null or empty");
            }

            if (atomMap.size() != 1) {
                throw new SkillCapsuleProcessingException("Each skill atom mapping must contain exactly one atom-sequence pair");
            }

            for (Map.Entry<UUID, Integer> entry : atomMap.entrySet()) {
                UUID atomId = entry.getKey();
                Integer sequence = getSequence(entry, atomId);

                // Check for duplicate atom IDs
                if (!atomIds.add(atomId)) {
                    throw new SkillCapsuleProcessingException("Duplicate skill atom ID found: " + atomId);
                }

                // Check for duplicate sequence orders
                if (!sequences.add(sequence)) {
                    throw new SkillCapsuleProcessingException("Duplicate sequence order found: " + sequence);
                }
            }
        }

        log.debug("Validated {} skill atom mappings for capsule {}", skillAtomMappings.size(), capsuleId);
    }

    private static Integer getSequence(Map.Entry<UUID, Integer> entry, UUID atomId) {
        Integer sequence = entry.getValue();

        // Validate atom ID
        if (atomId == null) {
            throw new SkillCapsuleProcessingException("Skill atom ID cannot be null");
        }

        // Validate sequence order
        if (sequence == null || sequence < 1) {
            throw new SkillCapsuleProcessingException("Sequence order must be a positive integer, got: " + sequence);
        }
        return sequence;
    }

    /**
     * Handle processing failures with DLT support
     */
    private void handleProcessingFailure(SkillCapsuleEvent event, Acknowledgment acknowledgment, String operationType) {
        String capsuleId = event.getId().toString();

        log.error("Processing failed for skill capsule {} operation with capsuleId: {}. Sending to DLT topic: {}",
                operationType, capsuleId, capsuleDltTopic);

        try {
            // Create enhanced event with operation type for DLT analysis
            Map<String, Object> dltPayload = Map.of(
                    "originalEvent", event,
                    "operationType", operationType,
                    "failureTimestamp", System.currentTimeMillis(),
                    "capsuleId", capsuleId
            );

            // Send to Dead Letter Topic for manual review/reprocessing
            kafkaTemplate.send(capsuleDltTopic, capsuleId, dltPayload)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent failed skill capsule {} event to DLT for capsuleId: {}",
                                    operationType, capsuleId);
                        } else {
                            log.error("Failed to send skill capsule {} event to DLT for capsuleId: {}",
                                    operationType, capsuleId, ex);
                        }
                    });

            // Acknowledge the original message to prevent infinite retries
            acknowledgment.acknowledge();

        } catch (Exception dltException) {
            log.error("Critical: Failed to send skill capsule {} message to DLT for capsuleId: {}. Message will be retried by Kafka",
                    operationType, capsuleId, dltException);
        }
    }

    @KafkaListener(topics = "${KAFKA_CAPSULE_UPDATE_TOPIC:capsule.update}")
    @Retryable(
            retryFor = {SkillCapsuleProcessingException.class, CapsuleAtomMappingException.class, Exception.class},
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void listenCapsuleUpdate(
            @Payload SkillCapsuleEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received capsule.update event from topic: {}, partition: {}, offset: {}, capsuleId: {}",
                topic, partition, offset, event.getId());

        try {
            // Validate event
            validateSkillCapsuleEvent(event);

            // Process the skill capsule update using existing service logic
            SkillCapsuleSnapshot updatedCapsule = skillCapsuleSnapshotService.processSkillCapsuleEvent(event);

            log.info("Successfully updated skill capsule for capsuleId: {}, internal ID: {}, atoms: {}",
                    event.getId(), updatedCapsule.getId(),
                    event.getSkillAtom() != null ? event.getSkillAtom().size() : 0);

            // Acknowledge message only after successful processing
            acknowledgment.acknowledge();

        } catch (SkillCapsuleProcessingException e) {
            log.error("Failed to update skill capsule for capsuleId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment, "UPDATE");

        } catch (CapsuleAtomMappingException e) {
            log.error("Failed to update capsule-atom mappings for capsuleId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment, "UPDATE");

        } catch (SkillAtomNotFoundException e) {
            log.error("Missing skill atom reference during capsule update for capsuleId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment, "UPDATE");

        } catch (Exception e) {
            log.error("Unexpected error updating skill capsule for capsuleId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment, "UPDATE");
        }
    }

    @KafkaListener(topics = "${KAFKA_CAPSULE_ASSIGN_TOPIC:capsule.assign}")
    @Retryable(
            retryFor = {SkillCapsuleProcessingException.class, CapsuleAtomMappingException.class, SkillAtomNotFoundException.class,
                    Exception.class},
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void listenCapsuleAssign(
            @Payload SkillCapsuleEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received capsule.assign event from topic: {}, partition: {}, offset: {}, capsuleId: {}",
                topic, partition, offset, event.getId());

        try {
            // Validate capsule exists
            validateCapsuleAssignEvent(event);

            // Process atom assignment to existing capsule
            SkillCapsuleSnapshot updatedCapsule = skillCapsuleSnapshotService.assignAtomsToCapsule(event);

            log.info("Successfully assigned atoms to capsule for capsuleId: {}, internal ID: {}, atoms: {}",
                    event.getId(), updatedCapsule.getId(),
                    event.getSkillAtom() != null ? event.getSkillAtom().size() : 0);

            // Acknowledge message only after successful processing
            acknowledgment.acknowledge();

        } catch (SkillCapsuleNotFoundException e) {
            log.error("Capsule not found for assignment, capsuleId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment, "ASSIGN");

        } catch (CapsuleAtomMappingException e) {
            log.error("Failed to assign capsule-atom mappings for capsuleId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment, "ASSIGN");

        } catch (SkillAtomNotFoundException e) {
            log.error("Missing skill atom reference during capsule assignment for capsuleId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment, "ASSIGN");

        } catch (Exception e) {
            log.error("Unexpected error assigning atoms to capsule for capsuleId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment, "ASSIGN");
        }
    }

    /**
     * Validate capsule.assign event - focuses on atom mappings only
     */
    private void validateCapsuleAssignEvent(SkillCapsuleEvent event) {
        log.debug("Validating capsule.assign event: {}", event);

        // Basic event validation
        if (event == null) {
            throw new SkillCapsuleProcessingException("Capsule assign event cannot be null");
        }

        if (event.getId() == null) {
            throw new SkillCapsuleProcessingException("Capsule assign event must contain a valid capsule ID");
        }

        // For assign operation, skillAtom mappings are required
        if (event.getSkillAtom() == null || event.getSkillAtom().isEmpty()) {
            throw new CapsuleAtomMappingException("Capsule assign event must contain at least one skill atom mapping");
        }

        // Validate atom mappings structure
        validateSkillAtomMappings(event.getSkillAtom(), event.getId());

        log.debug("Capsule assign event validation successful for capsuleId: {}", event.getId());
    }

}
