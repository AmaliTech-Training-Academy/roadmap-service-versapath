package com.capstone.service.impl;

import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.dto.response.TalentRouteResponseDto;
import com.capstone.exception.*;
import com.capstone.mapper.TalentRouteEventMapper;
import com.capstone.mapper.TalentRouteMapper;
import com.capstone.model.GrowthTrackSnapshot;
import com.capstone.model.RouteTrackMapping;
import com.capstone.model.TalentRouteSnapshot;
import com.capstone.repository.TalentRouteSnapshotRepository;
import com.capstone.service.GrowthTrackSnapshotService;
import com.capstone.service.TalentRouteSnapshotService;
import com.capstone.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.TalentRouteEvent;
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
public class TalentRouteSnapshotServiceImpl implements TalentRouteSnapshotService {

    private final TalentRouteSnapshotRepository talentRouteSnapshotRepository;
    private final TalentRouteEventMapper talentRouteEventMapper;
    private final TalentRouteMapper talentRouteMapper;
    private final GrowthTrackSnapshotService growthTrackSnapshotService;

    @Override
    @CacheEvict(value = {"talent-routes", "talent-routes-with-tracks", "talent-routes-search", "talent-route-single"}, allEntries = true)
    public TalentRouteSnapshot processTalentRouteEvent(TalentRouteEvent event) {
        log.info("Processing talent route event for routeId: {}", event.getId());

        try {
            Optional<TalentRouteSnapshot> existingRoute = talentRouteSnapshotRepository.findByTalentRouteId(event.getId());

            if (existingRoute.isPresent()) {
                log.info("Route exists, updating route with ID: {}", event.getId());
                return updateTalentRoute(existingRoute.get(), event);
            } else {
                log.info("Route does not exist, creating new route with ID: {}", event.getId());
                return createTalentRoute(event);
            }

        } catch (Exception e) {
            log.error("Error processing talent route event for routeId: {}", event.getId(), e);
            throw new TalentRouteProcessingException("Failed to process talent route event", e);
        }
    }

    @Override
    public TalentRouteSnapshot createTalentRoute(TalentRouteEvent event) {
        log.debug("Creating new talent route from event: {}", event);

        try {
            // Create basic route entity
            TalentRouteSnapshot newRoute = talentRouteEventMapper.toTalentRouteSnapshot(event);
            TalentRouteSnapshot savedRoute = talentRouteSnapshotRepository.save(newRoute);
            log.info("Successfully created talent route with ID: {}", savedRoute.getTalentRouteId());

            // Process track mappings
            if (event.getGrowthTracks() != null && !event.getGrowthTracks().isEmpty()) {
                smartUpdateRouteTrackMappings(savedRoute, event.getGrowthTracks());
                log.info("Successfully created {} track mappings for route {}",
                        event.getGrowthTracks().size(), savedRoute.getTalentRouteId());
            }

            return savedRoute;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when creating route with ID: {}", event.getId(), e);
            throw new DuplicateTalentRouteException(event.getId());
        } catch (Exception e) {
            log.error("Unexpected error creating route with ID: {}", event.getId(), e);
            throw new TalentRouteProcessingException("Failed to create talent route", e);
        }
    }

    @Override
    public TalentRouteSnapshot updateTalentRoute(TalentRouteSnapshot existingRoute, TalentRouteEvent event) {
        log.debug("Updating existing route {} with event data", existingRoute.getTalentRouteId());

        try {
            // Update basic route fields
            talentRouteEventMapper.updateTalentRouteSnapshot(event, existingRoute);
            TalentRouteSnapshot updatedRoute = talentRouteSnapshotRepository.save(existingRoute);

            // Smart update track mappings
            if (event.getGrowthTracks() != null && !event.getGrowthTracks().isEmpty()) {
                smartUpdateRouteTrackMappings(updatedRoute, event.getGrowthTracks());
                log.info("Successfully updated track mappings for route {}", updatedRoute.getTalentRouteId());
            }

            log.info("Successfully updated talent route with ID: {}", updatedRoute.getTalentRouteId());
            return updatedRoute;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when updating route with ID: {}", existingRoute.getTalentRouteId(), e);
            throw new TalentRouteProcessingException("Route update failed due to data constraint violation", e);
        } catch (Exception e) {
            log.error("Unexpected error updating route with ID: {}", existingRoute.getTalentRouteId(), e);
            throw new TalentRouteProcessingException("Failed to update talent route", e);
        }
    }

    @Override
    public void smartUpdateRouteTrackMappings(TalentRouteSnapshot route, List<Map<UUID, Integer>> growthTrackMappings) {
        log.debug("Smart updating track mappings for route: {}", route.getTalentRouteId());

        try {
            // PHASE 1: VERIFY ALL TRACKS EXIST - Fail Fast Strategy
            List<TrackSequencePair> newMappings = verifyAndParseTracks(growthTrackMappings);
            log.debug("Verified {} track mappings for route {}", newMappings.size(), route.getTalentRouteId());

            // PHASE 2: ANALYZE CHANGES - Smart Diff Algorithm
            UpdateAnalysis analysis = analyzeChanges(route, newMappings);
            log.debug("Analysis for route {}: {} to add, {} to update",
                    route.getTalentRouteId(), analysis.getToAdd().size(), analysis.getToUpdate().size());

            // PHASE 3: APPLY UPDATES - Atomic Operation
            applyRouteTrackMappingUpdates(route, analysis);

            log.info("Smart update completed for route {}: {} added, {} updated, {} preserved",
                    route.getTalentRouteId(), analysis.getToAdd().size(),
                    analysis.getToUpdate().size(), analysis.getPreserved());

        } catch (GrowthTrackNotFoundException e) {
            log.error("Track verification failed for route {}: {}", route.getTalentRouteId(), e.getMessage());
            throw new RouteTrackMappingException("Growth track not found: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Smart update failed for route {}: {}", route.getTalentRouteId(), e.getMessage(), e);
            throw new RouteTrackMappingException("Smart update failed", e);
        }
    }

    private List<TrackSequencePair> verifyAndParseTracks(List<Map<UUID, Integer>> growthTrackMappings) {
        List<TrackSequencePair> parsedMappings = new ArrayList<>();

        for (Map<UUID, Integer> trackMap : growthTrackMappings) {
            for (Map.Entry<UUID, Integer> entry : trackMap.entrySet()) {
                UUID trackId = entry.getKey();
                Integer sequence = entry.getValue();

                // Verify track exists
                GrowthTrackSnapshot track = growthTrackSnapshotService.findByGrowthTrackId(trackId)
                        .orElseThrow(() -> new GrowthTrackNotFoundException(trackId));

                // Validate sequence order
                if (sequence == null || sequence < 1) {
                    throw new RouteTrackMappingException(null, trackId, "Invalid sequence order: " + sequence);
                }

                parsedMappings.add(new TrackSequencePair(track, sequence));
            }
        }

        return parsedMappings;
    }

    private UpdateAnalysis analyzeChanges(TalentRouteSnapshot route, List<TrackSequencePair> newMappings) {
        // Build lookup map of existing mappings
        Map<UUID, RouteTrackMapping> existingMap = route.getRouteTrackMappings()
                .stream()
                .collect(Collectors.toMap(
                        mapping -> mapping.getGrowthTrack().getGrowthTrackId(),
                        mapping -> mapping
                ));

        List<RouteTrackMapping> toAdd = new ArrayList<>();
        List<RouteTrackMapping> toUpdate = new ArrayList<>();
        int preserved = 0;

        // Process each new mapping
        for (TrackSequencePair newMapping : newMappings) {
            UUID trackId = newMapping.getTrack().getGrowthTrackId();

            if (existingMap.containsKey(trackId)) {
                // TRACK EXISTS - Check if sequence changed
                RouteTrackMapping existing = existingMap.get(trackId);
                if (!existing.getSequenceOrder().equals(newMapping.getSequence())) {
                    existing.setSequenceOrder(newMapping.getSequence());
                    toUpdate.add(existing);
                } else {
                    preserved++; // No change needed
                }
                // Remove from map to track what remains
                existingMap.remove(trackId);
            } else {
                // NEW TRACK - Create mapping
                RouteTrackMapping newMappingEntity = RouteTrackMapping.builder()
                        .talentRoute(route)
                        .growthTrack(newMapping.getTrack())
                        .sequenceOrder(newMapping.getSequence())
                        .build();
                toAdd.add(newMappingEntity);
            }
        }

        // Remaining mappings in existingMap are preserved (not in new event)
        preserved += existingMap.size();

        return new UpdateAnalysis(toAdd, toUpdate, preserved);
    }

    private void applyRouteTrackMappingUpdates(TalentRouteSnapshot route, UpdateAnalysis analysis) {
        // Add new mappings
        if (!analysis.getToAdd().isEmpty()) {
            route.getRouteTrackMappings().addAll(analysis.getToAdd());
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "talent-routes", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public PaginatedResponseDto<TalentRouteResponseDto> findAllBasic(Pageable pageable) {
        log.debug("Finding all talent routes (basic) with pagination: page={}, size={}",
            pageable.getPageNumber(), pageable.getPageSize());

        Page<TalentRouteSnapshot> pageData = talentRouteSnapshotRepository.findAll(pageable);
        Page<TalentRouteResponseDto> dtoPage = pageData.map(talentRouteMapper::toBasicResponseDto);
        return PaginationUtil.toPaginatedResponse(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "talent-routes-with-tracks", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public PaginatedResponseDto<TalentRouteResponseDto> findAllWithTrackSummaries(Pageable pageable) {
        log.debug("Finding all talent routes with track summaries: page={}, size={}",
            pageable.getPageNumber(), pageable.getPageSize());

        Page<TalentRouteSnapshot> pageData = talentRouteSnapshotRepository.findAllWithTrackMappings(pageable);
        Page<TalentRouteResponseDto> dtoPage = pageData.map(talentRouteMapper::toResponseDtoWithTracks);
        return PaginationUtil.toPaginatedResponse(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "talent-routes-search", key = "#routeName + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public PaginatedResponseDto<TalentRouteResponseDto> searchByRouteNameBasic(String routeName, Pageable pageable) {
        log.debug("Searching talent routes (basic) by name '{}': page={}, size={}",
            routeName, pageable.getPageNumber(), pageable.getPageSize());

        Page<TalentRouteSnapshot> pageData = talentRouteSnapshotRepository.findByRouteNameContainingIgnoreCase(routeName, pageable);
        Page<TalentRouteResponseDto> dtoPage = pageData.map(talentRouteMapper::toBasicResponseDto);
        return PaginationUtil.toPaginatedResponse(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "talent-route-single", key = "#talentRouteId")
    public Optional<TalentRouteResponseDto> findByTalentRouteIdBasic(UUID talentRouteId) {
        log.debug("Finding talent route (basic) by talentRouteId: {}", talentRouteId);

        return talentRouteSnapshotRepository.findByTalentRouteId(talentRouteId)
                .map(talentRouteMapper::toBasicResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "talent-route-single", key = "#talentRouteId + '-with-tracks'")
    public Optional<TalentRouteResponseDto> findByTalentRouteIdWithTrackSummaries(UUID talentRouteId) {
        log.debug("Finding talent route with track summaries by talentRouteId: {}", talentRouteId);

        return talentRouteSnapshotRepository.findByTalentRouteIdWithTrackMappings(talentRouteId)
                .map(talentRouteMapper::toResponseDtoWithTracks);
    }

    // Helper classes
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class TrackSequencePair {
        private GrowthTrackSnapshot track;
        private Integer sequence;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class UpdateAnalysis {
        private List<RouteTrackMapping> toAdd;
        private List<RouteTrackMapping> toUpdate;
        private int preserved;
    }
}