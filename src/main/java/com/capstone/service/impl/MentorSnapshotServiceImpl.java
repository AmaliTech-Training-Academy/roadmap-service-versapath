package com.capstone.service.impl;

import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.dto.response.MentorResponseDto;
import com.capstone.exception.*;
import com.capstone.mapper.MentorEventMapper;
import com.capstone.mapper.MentorMapper;
import com.capstone.model.MentorSnapshot;
import com.capstone.model.MentorRouteMapping;
import com.capstone.model.TalentRouteSnapshot;
import com.capstone.repository.MentorSnapshotRepository;
import com.capstone.service.MentorSnapshotService;
import com.capstone.service.TalentRouteSnapshotService;
import com.capstone.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.ProduceMentorEvent;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MentorSnapshotServiceImpl implements MentorSnapshotService {

    private final MentorSnapshotRepository mentorSnapshotRepository;
    private final MentorEventMapper mentorEventMapper;
    private final MentorMapper mentorMapper;
    private final TalentRouteSnapshotService talentRouteSnapshotService;

    @Override
    @CacheEvict(value = {
            "mentors", "mentors-with-specializations", "mentors-search", "mentor-single"
    }, allEntries = true)
    public MentorSnapshot processMentorEvent(ProduceMentorEvent event) {
        log.info("Processing mentor event for mentorId: {}", event.getVersapathUserId());

        try {
            Optional<MentorSnapshot> existingMentor = mentorSnapshotRepository.findByMentorId(event.getVersapathUserId());

            if (existingMentor.isPresent()) {
                log.info("Mentor exists, updating mentor with ID: {}", event.getVersapathUserId());
                return updateMentor(existingMentor.get(), event);
            } else {
                log.info("Mentor does not exist, creating new mentor with ID: {}", event.getVersapathUserId());
                return createMentor(event);
            }

        } catch (Exception e) {
            log.error("Error processing mentor event for mentorId: {}", event.getVersapathUserId(), e);
            throw new MentorProcessingException("Failed to process mentor event", e);
        }
    }

    @Override
    public MentorSnapshot createMentor(ProduceMentorEvent event) {
        log.debug("Creating new mentor from event: {}", event);

        try {
            // Create basic mentor entity
            MentorSnapshot newMentor = mentorEventMapper.toMentorSnapshot(event);
            MentorSnapshot savedMentor = mentorSnapshotRepository.save(newMentor);
            log.info("Successfully created mentor with ID: {}", savedMentor.getMentorId());

            // Process specialization mappings
            if (event.getSpecializations() != null && !event.getSpecializations().isEmpty()) {
                smartUpdateMentorRouteMappings(savedMentor, event.getSpecializations());
                log.info("Successfully created {} specialization mappings for mentor {}",
                        event.getSpecializations().size(), savedMentor.getMentorId());
            }

            return savedMentor;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when creating mentor with ID: {}", event.getVersapathUserId(), e);
            throw new DuplicateMentorException(event.getVersapathUserId());
        } catch (Exception e) {
            log.error("Unexpected error creating mentor with ID: {}", event.getVersapathUserId(), e);
            throw new MentorProcessingException("Failed to create mentor", e);
        }
    }

    @Override
    public MentorSnapshot updateMentor(MentorSnapshot existingMentor, ProduceMentorEvent event) {
        log.debug("Updating existing mentor {} with event data", existingMentor.getMentorId());

        try {
            // Update basic mentor fields
            mentorEventMapper.updateMentorSnapshot(event, existingMentor);
            MentorSnapshot updatedMentor = mentorSnapshotRepository.save(existingMentor);

            // Smart update specialization mappings
            if (event.getSpecializations() != null && !event.getSpecializations().isEmpty()) {
                smartUpdateMentorRouteMappings(updatedMentor, event.getSpecializations());
                log.info("Successfully updated specialization mappings for mentor {}", updatedMentor.getMentorId());
            }

            log.info("Successfully updated mentor with ID: {}", updatedMentor.getMentorId());
            return updatedMentor;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when updating mentor with ID: {}", existingMentor.getMentorId(), e);
            throw new MentorProcessingException("Mentor update failed due to data constraint violation", e);
        } catch (Exception e) {
            log.error("Unexpected error updating mentor with ID: {}", existingMentor.getMentorId(), e);
            throw new MentorProcessingException("Failed to update mentor", e);
        }
    }

    @Override
    public void smartUpdateMentorRouteMappings(MentorSnapshot mentor, List<UUID> specializationRouteIds) {
        log.debug("Smart updating specialization mappings for mentor: {}", mentor.getMentorId());

        try {
            // PHASE 1: VERIFY ALL ROUTES EXIST - Fail Fast Strategy
            List<TalentRouteSnapshot> verifiedRoutes = verifySpecializationRoutes(specializationRouteIds);
            log.debug("Verified {} specialization mappings for mentor {}", verifiedRoutes.size(), mentor.getMentorId());

            // PHASE 2: ANALYZE CHANGES - Smart Diff Algorithm
            UpdateAnalysis analysis = analyzeMentorRouteChanges(mentor, verifiedRoutes);
            log.debug("Analysis for mentor {}: {} to add, {} preserved",
                    mentor.getMentorId(), analysis.getToAdd().size(), analysis.getPreserved());

            // PHASE 3: APPLY UPDATES - Atomic Operation
            applyMentorRouteMappingUpdates(mentor, analysis);

            log.info("Smart update completed for mentor {}: {} added, {} preserved",
                    mentor.getMentorId(), analysis.getToAdd().size(), analysis.getPreserved());

        } catch (TalentRouteNotFoundException e) {
            log.error("Route verification failed for mentor {}: {}", mentor.getMentorId(), e.getMessage());
            throw new MentorRouteMappingException("Talent route not found: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Smart update failed for mentor {}: {}", mentor.getMentorId(), e.getMessage(), e);
            throw new MentorRouteMappingException("Smart update failed", e);
        }
    }

    @Override
    @CacheEvict(value = {"mentors", "mentors-with-specializations", "mentors-search", "mentor-single"}, allEntries = true)
    public MentorSnapshot assignSpecializationsToMentor(ProduceMentorEvent event) {
        log.info("Assigning specializations to mentor for mentorId: {}", event.getVersapathUserId());

        try {
            // Find existing mentor WITH route mappings
            Optional<MentorSnapshot> existingMentor = mentorSnapshotRepository.findByMentorIdWithRouteMappings(event.getVersapathUserId());

            if (existingMentor.isEmpty()) {
                throw new MentorNotFoundException(event.getVersapathUserId());
            }

            MentorSnapshot mentor = existingMentor.get();
            log.info("Found existing mentor for assignment: {} with {} existing specialization mappings",
                    mentor.getMentorId(), mentor.getMentorRouteMappings().size());

            // Use existing smart update logic to assign specializations
            if (event.getSpecializations() != null && !event.getSpecializations().isEmpty()) {
                smartUpdateMentorRouteMappings(mentor, event.getSpecializations());

                // Save the updated mentor
                MentorSnapshot updatedMentor = mentorSnapshotRepository.save(mentor);

                log.info("Successfully assigned {} specializations to mentor {}, total mappings now: {}",
                        event.getSpecializations().size(), updatedMentor.getMentorId(),
                        updatedMentor.getMentorRouteMappings().size());

                return updatedMentor;
            } else {
                throw new MentorRouteMappingException("No specialization mappings provided for assignment");
            }

        } catch (MentorNotFoundException e) {
            log.error("Mentor not found for assignment with ID: {}", event.getVersapathUserId(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error assigning specializations to mentor with ID: {}", event.getVersapathUserId(), e);
            throw new MentorProcessingException("Failed to assign specializations to mentor", e);
        }
    }

    private List<TalentRouteSnapshot> verifySpecializationRoutes(List<UUID> specializationRouteIds) {
        List<TalentRouteSnapshot> verifiedRoutes = new ArrayList<>();

        for (UUID routeId : specializationRouteIds) {
            // Use TalentRouteSnapshotService to verify route exists
            Optional<TalentRouteSnapshot> route = talentRouteSnapshotService.findByTalentRouteId(routeId);
            if (route.isEmpty()) {
                throw new TalentRouteNotFoundException(routeId);
            }
            verifiedRoutes.add(route.get());
        }

        return verifiedRoutes;
    }

    private UpdateAnalysis analyzeMentorRouteChanges(MentorSnapshot mentor, List<TalentRouteSnapshot> newRoutes) {
        // Build lookup map of existing mappings
        Map<UUID, MentorRouteMapping> existingMap = mentor.getMentorRouteMappings()
                .stream()
                .collect(Collectors.toMap(
                        mapping -> mapping.getTalentRoute().getTalentRouteId(),
                        mapping -> mapping
                ));

        List<MentorRouteMapping> toAdd = new ArrayList<>();
        int preserved = 0;

        // Process each new route
        for (TalentRouteSnapshot route : newRoutes) {
            UUID routeId = route.getTalentRouteId();

            if (existingMap.containsKey(routeId)) {
                preserved++; // Already exists
                existingMap.remove(routeId);
            } else {
                // NEW ROUTE - Create mapping
                MentorRouteMapping newMapping = MentorRouteMapping.builder()
                        .mentorSnapshot(mentor)
                        .talentRoute(route)
                        .build();
                toAdd.add(newMapping);
            }
        }

        // Remaining mappings in existingMap are preserved (not in new event)
        preserved += existingMap.size();

        return new UpdateAnalysis(toAdd, preserved);
    }

    private void applyMentorRouteMappingUpdates(MentorSnapshot mentor, UpdateAnalysis analysis) {
        // Add new mappings
        if (!analysis.getToAdd().isEmpty()) {
            mentor.getMentorRouteMappings().addAll(analysis.getToAdd());
        }
    }

    // Query methods implementation
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "mentors", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public PaginatedResponseDto<MentorResponseDto> findAllBasic(Pageable pageable) {
        log.debug("Finding all mentors (basic) with pagination: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<MentorSnapshot> pageData = mentorSnapshotRepository.findAll(pageable);
        Page<MentorResponseDto> dtoPage = pageData.map(mentorMapper::toBasicResponseDto);
        return PaginationUtil.toPaginatedResponse(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "mentors-with-specializations", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public PaginatedResponseDto<MentorResponseDto> findAllWithSpecializations(Pageable pageable) {
        log.debug("Finding all mentors with specializations: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<MentorSnapshot> pageData = mentorSnapshotRepository.findAllWithRouteMappings(pageable);
        Page<MentorResponseDto> dtoPage = pageData.map(mentorMapper::toResponseDtoWithSpecializations);
        return PaginationUtil.toPaginatedResponse(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "mentors-search", key = "#name + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public PaginatedResponseDto<MentorResponseDto> searchByNameBasic(String name, Pageable pageable) {
        log.debug("Searching mentors (basic) by name '{}': page={}, size={}",
                name, pageable.getPageNumber(), pageable.getPageSize());

        Page<MentorSnapshot> pageData = mentorSnapshotRepository.findByNameContainingIgnoreCase(name, pageable);
        Page<MentorResponseDto> dtoPage = pageData.map(mentorMapper::toBasicResponseDto);
        return PaginationUtil.toPaginatedResponse(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "mentor-single", key = "#mentorId")
    public Optional<MentorResponseDto> findByMentorIdBasic(UUID mentorId) {
        log.debug("Finding mentor by ID (basic): {}", mentorId);

        return mentorSnapshotRepository.findByMentorId(mentorId)
                .map(mentorMapper::toBasicResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "mentor-single", key = "#mentorId + '-with-specializations'")
    public Optional<MentorResponseDto> findByMentorIdWithSpecializations(UUID mentorId) {
        log.debug("Finding mentor by ID with specializations: {}", mentorId);

        return mentorSnapshotRepository.findByMentorIdWithRouteMappings(mentorId)
                .map(mentorMapper::toResponseDtoWithSpecializations);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "mentors-by-specialization", key = "#talentRouteId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public PaginatedResponseDto<MentorResponseDto> findBySpecialization(UUID talentRouteId, Pageable pageable) {
        log.debug("Finding mentors by specialization: {}, page={}, size={}",
                talentRouteId, pageable.getPageNumber(), pageable.getPageSize());

        Page<MentorSnapshot> pageData = mentorSnapshotRepository.findBySpecializationTalentRouteId(talentRouteId, pageable);
        Page<MentorResponseDto> dtoPage = pageData.map(mentorMapper::toBasicResponseDto);
        return PaginationUtil.toPaginatedResponse(dtoPage);
    }

    // Helper classes
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class UpdateAnalysis {
        private List<MentorRouteMapping> toAdd;
        private int preserved;
    }
}
