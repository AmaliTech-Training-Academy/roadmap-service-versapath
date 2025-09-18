package com.capstone.messaging;

import com.capstone.exception.GrowthTrackNotFoundException;
import com.capstone.exception.RouteTrackMappingException;
import com.capstone.exception.TalentRouteProcessingException;
import com.capstone.model.TalentRouteSnapshot;
import com.capstone.service.TalentRouteSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.TalentRouteEvent;
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
public class TalentRouteKafkaConsumer {

    private final TalentRouteSnapshotService talentRouteSnapshotService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.talentRoute-dlt-topic:talentRoute.create.dlt}")
    private String talentRouteDltTopic;

    @KafkaListener(topics = "${KAFKA_TALENT_ROUTE_TOPIC:talentRoute.create}")
    @Retryable(
            retryFor = {TalentRouteProcessingException.class, RouteTrackMappingException.class, Exception.class},
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void listenTalentRouteCreate(
            @Payload TalentRouteEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received talent route event from topic: {}, partition: {}, offset: {}, routeId: {}",
                topic, partition, offset, event.getId());

        try {
            // Validate event
            validateTalentRouteEvent(event);

            // Process the talent route event
            TalentRouteSnapshot processedRoute = talentRouteSnapshotService.processTalentRouteEvent(event);

            log.info("Successfully processed talent route event for routeId: {}, internal ID: {}, tracks: {}",
                    event.getId(), processedRoute.getId(),
                    event.getGrowthTracks() != null ? event.getGrowthTracks().size() : 0);

            // Acknowledge message only after successful processing
            acknowledgment.acknowledge();

        } catch (TalentRouteProcessingException e) {
            log.error("Failed to process talent route event for routeId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment);

        } catch (RouteTrackMappingException e) {
            log.error("Failed to process route-track mappings for routeId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment);

        } catch (GrowthTrackNotFoundException e) {
            log.error("Missing growth track reference for routeId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment);

        } catch (Exception e) {
            log.error("Unexpected error processing talent route event for routeId: {}. Error: {}",
                    event.getId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment);
        }
    }

    /**
     * Comprehensive validation of TalentRouteEvent
     */
    private void validateTalentRouteEvent(TalentRouteEvent event) {
        log.debug("Validating talent route event: {}", event);

        // Basic event validation
        if (event == null) {
            throw new TalentRouteProcessingException("Talent route event cannot be null");
        }

        if (event.getId() == null) {
            throw new TalentRouteProcessingException("Talent route event must contain a valid ID");
        }

        if (event.getName() == null || event.getName().trim().isEmpty()) {
            throw new TalentRouteProcessingException("Talent route event must contain a valid name");
        }

        // Validate growthTrack list structure
        if (event.getGrowthTracks() != null) {
            validateGrowthTrackMappings(event.getGrowthTracks(), event.getId());
        }

        log.debug("Talent route event validation successful for routeId: {}", event.getId());
    }

    /**
     * Validate growthTrack mapping structure and business rules
     */
    private void validateGrowthTrackMappings(List<Map<UUID, Integer>> growthTrackMappings, UUID routeId) {
        if (growthTrackMappings.isEmpty()) {
            log.warn("Talent route {} has empty growthTrack list - no learning path defined", routeId);
            return;
        }

        Set<UUID> trackIds = new HashSet<>();
        Set<Integer> sequences = new HashSet<>();

        for (Map<UUID, Integer> trackMap : growthTrackMappings) {
            if (trackMap == null || trackMap.isEmpty()) {
                throw new TalentRouteProcessingException("Growth track mapping cannot be null or empty");
            }

            if (trackMap.size() != 1) {
                throw new TalentRouteProcessingException("Each growth track mapping must contain exactly one track-sequence pair");
            }

            for (Map.Entry<UUID, Integer> entry : trackMap.entrySet()) {
                UUID trackId = entry.getKey();
                Integer sequence = getSequence(entry, trackId);

                // Check for duplicate track IDs
                if (!trackIds.add(trackId)) {
                    throw new TalentRouteProcessingException("Duplicate growth track ID found: " + trackId);
                }

                // Check for duplicate sequence orders
                if (!sequences.add(sequence)) {
                    throw new TalentRouteProcessingException("Duplicate sequence order found: " + sequence);
                }
            }
        }

        log.debug("Validated {} growth track mappings for route {}", growthTrackMappings.size(), routeId);
    }

    private static Integer getSequence(Map.Entry<UUID, Integer> entry, UUID trackId) {
        Integer sequence = entry.getValue();

        // Validate track ID
        if (trackId == null) {
            throw new TalentRouteProcessingException("Growth track ID cannot be null");
        }

        // Validate sequence order
        if (sequence == null || sequence < 1) {
            throw new TalentRouteProcessingException("Sequence order must be a positive integer, got: " + sequence);
        }
        return sequence;
    }

    /**
     * Handle processing failures with DLT support
     */
    private void handleProcessingFailure(TalentRouteEvent event, Acknowledgment acknowledgment) {
        String routeId = event.getId().toString();

        log.error("Processing failed for talent route event with routeId: {}. Sending to DLT topic: {}",
                routeId, talentRouteDltTopic);

        try {
            // Send to Dead Letter Topic for manual review/reprocessing
            kafkaTemplate.send(talentRouteDltTopic, routeId, event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent failed talent route event to DLT for routeId: {}", routeId);
                        } else {
                            log.error("Failed to send talent route event to DLT for routeId: {}", routeId, ex);
                        }
                    });

            // Acknowledge the original message to prevent infinite retries
            acknowledgment.acknowledge();

        } catch (Exception dltException) {
            log.error("Critical: Failed to send talent route message to DLT for routeId: {}. Message will be retried by Kafka",
                    routeId, dltException);
        }
    }
}