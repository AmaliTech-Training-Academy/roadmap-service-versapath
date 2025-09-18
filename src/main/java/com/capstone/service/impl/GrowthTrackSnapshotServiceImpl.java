package com.capstone.service.impl;

import com.capstone.dto.response.GrowthTrackResponseDto;
import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.exception.*;
import com.capstone.mapper.GrowthTrackEventMapper;
import com.capstone.mapper.GrowthTrackMapper;
import com.capstone.model.GrowthTrackSnapshot;
import com.capstone.model.SkillCapsuleSnapshot;
import com.capstone.model.TrackCapsuleMapping;
import com.capstone.repository.GrowthTrackSnapshotRepository;
import com.capstone.service.GrowthTrackSnapshotService;
import com.capstone.service.SkillCapsuleSnapshotService;
import com.capstone.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.GrowthTrackEvent;
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
public class GrowthTrackSnapshotServiceImpl implements GrowthTrackSnapshotService {

    private final GrowthTrackSnapshotRepository growthTrackSnapshotRepository;
    private final GrowthTrackEventMapper growthTrackEventMapper;
    private final GrowthTrackMapper growthTrackMapper;
    private final SkillCapsuleSnapshotService skillCapsuleSnapshotService;

    @Override
    public GrowthTrackSnapshot processGrowthTrackEvent(GrowthTrackEvent event) {
        log.info("Processing growth track event for trackId: {}", event.getId());

        try {
            Optional<GrowthTrackSnapshot> existingTrack = growthTrackSnapshotRepository.findByGrowthTrackId(event.getId());

            if (existingTrack.isPresent()) {
                log.info("Track exists, updating track with ID: {}", event.getId());
                return updateGrowthTrack(existingTrack.get(), event);
            } else {
                log.info("Track does not exist, creating new track with ID: {}", event.getId());
                return createGrowthTrack(event);
            }

        } catch (Exception e) {
            log.error("Error processing growth track event for trackId: {}", event.getId(), e);
            throw new GrowthTrackProcessingException("Failed to process growth track event", e);
        }
    }

    @Override
    public GrowthTrackSnapshot createGrowthTrack(GrowthTrackEvent event) {
        log.debug("Creating new growth track from event: {}", event);

        try {
            // Create basic track entity
            GrowthTrackSnapshot newTrack = growthTrackEventMapper.toGrowthTrackSnapshot(event);
            GrowthTrackSnapshot savedTrack = growthTrackSnapshotRepository.save(newTrack);
            log.info("Successfully created growth track with ID: {}", savedTrack.getGrowthTrackId());

            // Process capsule mappings
            if (event.getSkillCapsules() != null && !event.getSkillCapsules().isEmpty()) {
                smartUpdateTrackCapsuleMappings(savedTrack, event.getSkillCapsules());
                log.info("Successfully created {} capsule mappings for track {}",
                        event.getSkillCapsules().size(), savedTrack.getGrowthTrackId());
            }

            return savedTrack;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when creating track with ID: {}", event.getId(), e);
            throw new DuplicateGrowthTrackException(event.getId());
        } catch (Exception e) {
            log.error("Unexpected error creating track with ID: {}", event.getId(), e);
            throw new GrowthTrackProcessingException("Failed to create growth track", e);
        }
    }

    @Override
    public GrowthTrackSnapshot updateGrowthTrack(GrowthTrackSnapshot existingTrack, GrowthTrackEvent event) {
        log.debug("Updating existing track {} with event data", existingTrack.getGrowthTrackId());

        try {
            // Update basic track fields
            growthTrackEventMapper.updateGrowthTrackSnapshot(event, existingTrack);
            GrowthTrackSnapshot updatedTrack = growthTrackSnapshotRepository.save(existingTrack);

            // Smart update capsule mappings
            if (event.getSkillCapsules() != null && !event.getSkillCapsules().isEmpty()) {
                smartUpdateTrackCapsuleMappings(updatedTrack, event.getSkillCapsules());
                log.info("Successfully updated capsule mappings for track {}", updatedTrack.getGrowthTrackId());
            }

            log.info("Successfully updated growth track with ID: {}", updatedTrack.getGrowthTrackId());
            return updatedTrack;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when updating track with ID: {}", existingTrack.getGrowthTrackId(), e);
            throw new GrowthTrackProcessingException("Track update failed due to data constraint violation", e);
        } catch (Exception e) {
            log.error("Unexpected error updating track with ID: {}", existingTrack.getGrowthTrackId(), e);
            throw new GrowthTrackProcessingException("Failed to update growth track", e);
        }
    }

    @Override
    public void smartUpdateTrackCapsuleMappings(GrowthTrackSnapshot track, List<Map<UUID, Integer>> skillCapsuleMappings) {
        log.debug("Smart updating capsule mappings for track: {}", track.getGrowthTrackId());

        try {
            // PHASE 1: VERIFY ALL CAPSULES EXIST - Fail Fast Strategy
            List<CapsuleSequencePair> newMappings = verifyAndParseCapsules(skillCapsuleMappings);
            log.debug("Verified {} capsule mappings for track {}", newMappings.size(), track.getGrowthTrackId());

            // PHASE 2: ANALYZE CHANGES - Smart Diff Algorithm
            UpdateAnalysis analysis = analyzeChanges(track, newMappings);
            log.debug("Analysis for track {}: {} to add, {} to update",
                    track.getGrowthTrackId(), analysis.getToAdd().size(), analysis.getToUpdate().size());

            // PHASE 3: APPLY UPDATES - Atomic Operation
            applyTrackCapsuleMappingUpdates(track, analysis);

            log.info("Smart update completed for track {}: {} added, {} updated, {} preserved",
                    track.getGrowthTrackId(), analysis.getToAdd().size(),
                    analysis.getToUpdate().size(), analysis.getPreserved());

        } catch (SkillCapsuleNotFoundException e) {
            log.error("Capsule verification failed for track {}: {}", track.getGrowthTrackId(), e.getMessage());
            throw new TrackCapsuleMappingException( "Capsule not found: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Smart update failed for track {}: {}", track.getGrowthTrackId(), e.getMessage(), e);
            throw new TrackCapsuleMappingException( "Smart update failed", e);
        }
    }

    private List<CapsuleSequencePair> verifyAndParseCapsules(List<Map<UUID, Integer>> skillCapsuleMappings) {
        List<CapsuleSequencePair> parsedMappings = new ArrayList<>();

        for (Map<UUID, Integer> capsuleMap : skillCapsuleMappings) {
            for (Map.Entry<UUID, Integer> entry : capsuleMap.entrySet()) {
                UUID capsuleId = entry.getKey();
                Integer sequence = entry.getValue();

                // Verify capsule exists
                SkillCapsuleSnapshot capsule = skillCapsuleSnapshotService.findBySkillCapsuleId(capsuleId)
                        .orElseThrow(() -> new SkillCapsuleNotFoundException(capsuleId));

                // Validate sequence order
                if (sequence == null || sequence < 1) {
                    throw new TrackCapsuleMappingException(null, capsuleId, "Invalid sequence order: " + sequence);
                }

                parsedMappings.add(new CapsuleSequencePair(capsule, sequence));
            }
        }

        return parsedMappings;
    }

    private UpdateAnalysis analyzeChanges(GrowthTrackSnapshot track, List<CapsuleSequencePair> newMappings) {
        // Build lookup map of existing mappings
        Map<UUID, TrackCapsuleMapping> existingMap = track.getTrackCapsuleMappings()
                .stream()
                .collect(Collectors.toMap(
                        mapping -> mapping.getSkillCapsule().getSkillCapsuleId(),
                        mapping -> mapping
                ));

        List<TrackCapsuleMapping> toAdd = new ArrayList<>();
        List<TrackCapsuleMapping> toUpdate = new ArrayList<>();
        int preserved = 0;

        // Process each new mapping
        for (CapsuleSequencePair newMapping : newMappings) {
            UUID capsuleId = newMapping.getCapsule().getSkillCapsuleId();

            if (existingMap.containsKey(capsuleId)) {
                // CAPSULE EXISTS - Check if sequence changed
                TrackCapsuleMapping existing = existingMap.get(capsuleId);
                if (!existing.getSequenceOrder().equals(newMapping.getSequence())) {
                    existing.setSequenceOrder(newMapping.getSequence());
                    toUpdate.add(existing);
                } else {
                    preserved++; // No change needed
                }
                // Remove from map to track what remains
                existingMap.remove(capsuleId);
            } else {
                // NEW CAPSULE - Create mapping
                TrackCapsuleMapping newMappingEntity = TrackCapsuleMapping.builder()
                        .growthTrack(track)
                        .skillCapsule(newMapping.getCapsule())
                        .sequenceOrder(newMapping.getSequence())
                        .build();
                toAdd.add(newMappingEntity);
            }
        }

        // Remaining mappings in existingMap are preserved (not in new event)
        preserved += existingMap.size();

        return new UpdateAnalysis(toAdd, toUpdate, preserved);
    }

    private void applyTrackCapsuleMappingUpdates(GrowthTrackSnapshot track, UpdateAnalysis analysis) {
        // Add new mappings
        if (!analysis.getToAdd().isEmpty()) {
            track.getTrackCapsuleMappings().addAll(analysis.getToAdd());
        }
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<GrowthTrackSnapshot> findByGrowthTrackId(UUID growthTrackId) {
        log.debug("Finding track by growthTrackId: {}", growthTrackId);
        return growthTrackSnapshotRepository.findByGrowthTrackId(growthTrackId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByGrowthTrackId(UUID growthTrackId) {
        log.debug("Checking if track exists by growthTrackId: {}", growthTrackId);
        return growthTrackSnapshotRepository.existsByGrowthTrackId(growthTrackId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GrowthTrackSnapshot> findByGrowthTrackIdWithCapsuleMappings(UUID growthTrackId) {
        log.debug("Finding track with capsule mappings by growthTrackId: {}", growthTrackId);
        return growthTrackSnapshotRepository.findByGrowthTrackIdWithCapsuleMappings(growthTrackId);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<GrowthTrackResponseDto> findAllBasic(Pageable pageable) {
        log.debug("Finding all growth tracks with basic info, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<GrowthTrackSnapshot> pageData = growthTrackSnapshotRepository.findAll(pageable);
        Page<GrowthTrackResponseDto> dtoPage = pageData.map(growthTrackMapper::toBasicResponseDto);
        return PaginationUtil.toPaginatedResponse(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<GrowthTrackResponseDto> findAllWithCapsuleSummaries(Pageable pageable) {
        log.debug("Finding all growth tracks with capsule summaries, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<GrowthTrackSnapshot> pageData = growthTrackSnapshotRepository.findAllWithCapsuleMappings(pageable);
        Page<GrowthTrackResponseDto> dtoPage = pageData.map(growthTrackMapper::toResponseDtoWithCapsules);
        return PaginationUtil.toPaginatedResponse(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<GrowthTrackResponseDto> searchByTrackNameBasic(String trackName, Pageable pageable) {
        log.debug("Searching growth tracks by name: '{}', page: {}, size: {}", trackName, pageable.getPageNumber(), pageable.getPageSize());
        Page<GrowthTrackSnapshot> pageData = growthTrackSnapshotRepository.findByTrackNameContainingIgnoreCase(trackName, pageable);
        Page<GrowthTrackResponseDto> dtoPage = pageData.map(growthTrackMapper::toBasicResponseDto);
        return PaginationUtil.toPaginatedResponse(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GrowthTrackResponseDto> findByGrowthTrackIdBasic(UUID growthTrackId) {
        log.debug("Finding growth track by ID with basic info: {}", growthTrackId);
        return growthTrackSnapshotRepository.findByGrowthTrackId(growthTrackId)
                .map(growthTrackMapper::toBasicResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GrowthTrackResponseDto> findByGrowthTrackIdWithCapsuleSummaries(UUID growthTrackId) {
        log.debug("Finding growth track by ID with capsule summaries: {}", growthTrackId);
        return growthTrackSnapshotRepository.findByGrowthTrackIdWithCapsuleMappings(growthTrackId)
                .map(growthTrackMapper::toResponseDtoWithCapsules);
    }

    // Helper classes

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class CapsuleSequencePair {
        private SkillCapsuleSnapshot capsule;
        private Integer sequence;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class UpdateAnalysis {
        private List<TrackCapsuleMapping> toAdd;
        private List<TrackCapsuleMapping> toUpdate;
        private int preserved;
    }
}
