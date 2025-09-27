package com.capstone.messaging;

import com.capstone.exception.MentorNotFoundException;
import com.capstone.exception.MentorProcessingException;
import com.capstone.exception.MentorRouteMappingException;
import com.capstone.exception.TalentRouteNotFoundException;
import com.capstone.model.MentorSnapshot;
import com.capstone.service.MentorSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.ProduceMentorEvent;
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

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class MentorKafkaConsumer {

    private final MentorSnapshotService mentorSnapshotService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${KAFKA_MENTOR_DLT_TOPIC:mentor.create.dlt}")
    private String mentorDltTopic;

    @KafkaListener(topics = "${KAFKA_MENTOR_TOPIC:mentor.create}")
    @Retryable(
            retryFor = {MentorProcessingException.class, MentorRouteMappingException.class, Exception.class},
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void listenMentorCreate(
            @Payload ProduceMentorEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received mentor event from topic: {}, partition: {}, offset: {}, mentorId: {}",
                topic, partition, offset, event.getVersapathUserId());

        try {
            validateMentorEvent(event);
            MentorSnapshot processedMentor = mentorSnapshotService.processMentorEvent(event);

            log.info("Successfully processed mentor event for mentorId: {}, internal ID: {}, specializations: {}",
                    event.getVersapathUserId(), processedMentor.getId(),
                    event.getSpecializations() != null ? event.getSpecializations().size() : 0);

            acknowledgment.acknowledge();

        } catch (MentorProcessingException e) {
            log.error("Failed to process mentor event for mentorId: {}. Error: {}",
                    event.getVersapathUserId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment, "CREATE");
        } catch (MentorRouteMappingException e) {
            log.error("Failed to process mentor-route mappings for mentorId: {}. Error: {}",
                    event.getVersapathUserId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment, "CREATE");
        } catch (TalentRouteNotFoundException e) {
            log.error("Missing talent route reference for mentorId: {}. Error: {}",
                    event.getVersapathUserId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment, "CREATE");
        } catch (Exception e) {
            log.error("Unexpected error processing mentor event for mentorId: {}. Error: {}",
                    event.getVersapathUserId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment, "CREATE");
        }
    }

    @KafkaListener(topics = "${KAFKA_MENTOR_UPDATE_TOPIC:mentor.update}")
    @Retryable(
            retryFor = {MentorProcessingException.class, MentorRouteMappingException.class, TalentRouteNotFoundException.class, Exception.class},
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void listenMentorUpdate(
            @Payload ProduceMentorEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received mentor.update event from topic: {}, partition: {}, offset: {}, mentorId: {}",
                topic, partition, offset, event.getVersapathUserId());

        try {
            validateMentorEvent(event);
            MentorSnapshot updatedMentor = mentorSnapshotService.processMentorEvent(event);

            log.info("Successfully updated mentor for mentorId: {}, internal ID: {}, specializations: {}",
                    event.getVersapathUserId(), updatedMentor.getId(),
                    event.getSpecializations() != null ? event.getSpecializations().size() : 0);

            acknowledgment.acknowledge();

        } catch (MentorProcessingException | MentorRouteMappingException | TalentRouteNotFoundException e) {
            log.error("Failed to update mentor for mentorId: {}. Error: {}",
                    event.getVersapathUserId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment, "UPDATE");
        } catch (Exception e) {
            log.error("Unexpected error updating mentor for mentorId: {}. Error: {}",
                    event.getVersapathUserId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment, "UPDATE");
        }
    }

    @KafkaListener(topics = "${KAFKA_MENTOR_ASSIGN_TOPIC:mentor.assign}")
    @Retryable(
            retryFor = {MentorProcessingException.class, MentorRouteMappingException.class, TalentRouteNotFoundException.class, Exception.class},
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void listenMentorAssign(
            @Payload ProduceMentorEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received mentor.assign event from topic: {}, partition: {}, offset: {}, mentorId: {}",
                topic, partition, offset, event.getVersapathUserId());

        try {
            validateMentorAssignEvent(event);
            MentorSnapshot updatedMentor = mentorSnapshotService.assignSpecializationsToMentor(event);

            log.info("Successfully assigned specializations to mentor for mentorId: {}, internal ID: {}, specializations: {}",
                    event.getVersapathUserId(), updatedMentor.getId(),
                    event.getSpecializations() != null ? event.getSpecializations().size() : 0);

            acknowledgment.acknowledge();

        } catch (MentorNotFoundException e) {
            log.error("Mentor not found for assignment, mentorId: {}. Error: {}",
                    event.getVersapathUserId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment, "ASSIGN");
        } catch (MentorRouteMappingException | TalentRouteNotFoundException e) {
            log.error("Failed to assign specializations to mentor for mentorId: {}. Error: {}",
                    event.getVersapathUserId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment, "ASSIGN");
        } catch (Exception e) {
            log.error("Unexpected error assigning specializations to mentor for mentorId: {}. Error: {}",
                    event.getVersapathUserId(), e.getMessage(), e);
            handleProcessingFailure(event, acknowledgment, "ASSIGN");
        }
    }

    private void validateMentorEvent(ProduceMentorEvent event) {
        log.debug("Validating mentor event: {}", event);

        if (event == null) {
            throw new MentorProcessingException("Mentor event cannot be null");
        }

        if (event.getVersapathUserId() == null) {
            throw new MentorProcessingException("Mentor event must contain a valid versapathUserId");
        }

        if (event.getEmail() == null || event.getEmail().trim().isEmpty()) {
            throw new MentorProcessingException("Mentor event must contain a valid email");
        }

        if (event.getFirstName() == null || event.getFirstName().trim().isEmpty()) {
            throw new MentorProcessingException("Mentor event must contain a valid first name");
        }

        if (event.getLastName() == null || event.getLastName().trim().isEmpty()) {
            throw new MentorProcessingException("Mentor event must contain a valid last name");
        }

        if (event.getUsername() == null || event.getUsername().trim().isEmpty()) {
            throw new MentorProcessingException("Mentor event must contain a valid username");
        }

        log.debug("Mentor event validation successful for mentorId: {}", event.getVersapathUserId());
    }

    private void validateMentorAssignEvent(ProduceMentorEvent event) {
        log.debug("Validating mentor.assign event: {}", event);

        if (event == null) {
            throw new MentorProcessingException("Mentor assign event cannot be null");
        }

        if (event.getVersapathUserId() == null) {
            throw new MentorProcessingException("Mentor assign event must contain a valid mentorId");
        }

        if (event.getSpecializations() == null || event.getSpecializations().isEmpty()) {
            throw new MentorRouteMappingException("Mentor assign event must contain at least one specialization");
        }

        log.debug("Mentor assign event validation successful for mentorId: {}", event.getVersapathUserId());
    }

    private void handleProcessingFailure(ProduceMentorEvent event, Acknowledgment acknowledgment, String operationType) {
        String mentorId = event.getVersapathUserId().toString();

        log.error("Processing failed for mentor {} operation with mentorId: {}. Sending to DLT topic: {}",
                operationType, mentorId, mentorDltTopic);

        try {
            Map<String, Object> dltPayload = Map.of(
                    "originalEvent", event,
                    "operationType", operationType,
                    "failureTimestamp", System.currentTimeMillis(),
                    "mentorId", mentorId
            );

            kafkaTemplate.send(mentorDltTopic, mentorId, dltPayload)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Successfully sent failed mentor {} event to DLT for mentorId: {}",
                                    operationType, mentorId);
                        } else {
                            log.error("Failed to send mentor {} event to DLT for mentorId: {}",
                                    operationType, mentorId, ex);
                        }
                    });

            acknowledgment.acknowledge();

        } catch (Exception dltException) {
            log.error("Critical: Failed to send mentor {} message to DLT for mentorId: {}. Message will be retried by Kafka",
                    operationType, mentorId, dltException);
        }
    }
}
