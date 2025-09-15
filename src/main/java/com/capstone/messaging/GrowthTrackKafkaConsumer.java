package com.capstone.messaging;

import com.capstone.exception.GrowthTrackProcessingException;
import com.capstone.exception.SkillCapsuleNotFoundException;
import com.capstone.exception.TrackCapsuleMappingException;
import com.capstone.model.GrowthTrackSnapshot;
import com.capstone.service.GrowthTrackSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.GrowthTrackEvent;
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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class GrowthTrackKafkaConsumer {

    private final GrowthTrackSnapshotService growthTrackSnapshotService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.growthTrack-dlt-topic:growthTrack.create.dlt}")
    private String growthTrackDltTopic;

    @KafkaListener(topics = "${KAFKA_GROWTH_TRACK_TOPIC:growthTrack.create}")
    @Retryable(
            retryFor = {GrowthTrackProcessingException.class, TrackCapsuleMappingException.class, Exception.class},
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void listenGrowthTrackCreate(
            @Payload GrowthTrackEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received growth track event from topic: {}, partition: {}, offset: {}, trackId: {}",
                topic, partition, offset, event.getId());

        try {
            // Validate event
            validateGrowthTrackEvent(event);

            // Process the growth track event
            GrowthTrackSnapshot processedTrack = growthTrackSnapshotService.processGrowthTrackEvent(event);

            log.info("Successfully processed growth track event for trackId: {}, internal ID: {}, capsules: {}",
                    event.getId(), processedTrack.getId(),
                    event.getSkillCapsules() != null ? event.getSkillCapsules().size() : 0);

            // Acknowledge message only after successful processing
            acknowledgment.acknowledge();

        } catch (GrowthTrackProcessingException e) {
            log.error("Failed to process growth track event for trackId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment);

        } catch (TrackCapsuleMappingException e) {
            log.error("Failed to process track-capsule mappings for trackId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment);

        } catch (SkillCapsuleNotFoundException e) {
            log.error("Missing skill capsule reference for trackId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment);

        } catch (Exception e) {
            log.error("Unexpected error processing growth track event for trackId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment);
        }
    }

    /**
     * Comprehensive validation of GrowthTrackEvent
     */
    private void validateGrowthTrackEvent(GrowthTrackEvent event) {
        log.debug("Validating growth track event: {}", event);

        // Basic event validation
        if (event == null) {
            throw new GrowthTrackProcessingException("Growth track event cannot be null");
        }

        if (event.getId() == null) {
            throw new GrowthTrackProcessingException("Growth track event must contain a valid ID");
        }

        if (event.getName() == null || event.getName().trim().isEmpty()) {
            throw new GrowthTrackProcessingException("Growth track event must contain a valid name");
        }

        // Validate skillCapsule list structure
        if (event.getSkillCapsules() != null) {
            validateSkillCapsuleMappings(event.getSkillCapsules(), event.getId());
        }

        log.debug("Growth track event validation successful for trackId: {}", event.getId());
    }

    /**
     * Validate skillCapsule mapping structure and business rules
     */
    private void validateSkillCapsuleMappings(List<Map<UUID, Integer>> skillCapsuleMappings, UUID trackId) {
        if (skillCapsuleMappings.isEmpty()) {
            log.warn("Growth track {} has empty skillCapsule list - no learning path defined", trackId);
            return;
        }

        Set<UUID> capsuleIds = new HashSet<>();
        Set<Integer> sequences = new HashSet<>();

        for (Map<UUID, Integer> capsuleMap : skillCapsuleMappings) {
            if (capsuleMap == null || capsuleMap.isEmpty()) {
                throw new GrowthTrackProcessingException("Skill capsule mapping cannot be null or empty");
            }

            if (capsuleMap.size() != 1) {
                throw new GrowthTrackProcessingException("Each skill capsule mapping must contain exactly one capsule-sequence pair");
            }

            for (Map.Entry<UUID, Integer> entry : capsuleMap.entrySet()) {
                UUID capsuleId = entry.getKey();
                Integer sequence = getSequence(entry, capsuleId);

                // Check for duplicate capsule IDs
                if (!capsuleIds.add(capsuleId)) {
                    throw new GrowthTrackProcessingException("Duplicate skill capsule ID found: " + capsuleId);
                }

                // Check for duplicate sequence orders
                if (!sequences.add(sequence)) {
                    throw new GrowthTrackProcessingException("Duplicate sequence order found: " + sequence);
                }
            }
        }

        log.debug("Validated {} skill capsule mappings for track {}", skillCapsuleMappings.size(), trackId);
    }

    private static Integer getSequence(Map.Entry<UUID, Integer> entry, UUID capsuleId) {
        Integer sequence = entry.getValue();

        // Validate capsule ID
        if (capsuleId == null) {
            throw new GrowthTrackProcessingException("Skill capsule ID cannot be null");
        }

        // Validate sequence order
        if (sequence == null || sequence < 1) {
            throw new GrowthTrackProcessingException("Sequence order must be a positive integer, got: " + sequence);
        }
        return sequence;
    }

    /**
     * Handle processing failures with DLT support
     */
    private void handleProcessingFailure(GrowthTrackEvent event, Acknowledgment acknowledgment) {
        String trackId = event.getId().toString();

        log.error("Processing failed for growth track event with trackId: {}. Sending to DLT topic: {}",
                trackId, growthTrackDltTopic);

        try {
            // Send to Dead Letter Topic for manual review/reprocessing
            kafkaTemplate.send(growthTrackDltTopic, trackId, event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent failed growth track event to DLT for trackId: {}", trackId);
                        } else {
                            log.error("Failed to send growth track event to DLT for trackId: {}", trackId, ex);
                        }
                    });

            // Acknowledge the original message to prevent infinite retries
            acknowledgment.acknowledge();

        } catch (Exception dltException) {
            log.error("Critical: Failed to send growth track message to DLT for trackId: {}. Message will be retried by Kafka",
                    trackId, dltException);
        }
    }
}
