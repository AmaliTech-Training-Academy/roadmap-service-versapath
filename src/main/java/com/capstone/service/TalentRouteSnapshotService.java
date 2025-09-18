package com.capstone.service;

import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.dto.response.TalentRouteResponseDto;
import com.capstone.model.TalentRouteSnapshot;
import org.common.event.TalentRouteEvent;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface TalentRouteSnapshotService {
    // ===== EXISTING KAFKA/PROCESSING METHODS =====
    TalentRouteSnapshot processTalentRouteEvent(TalentRouteEvent event);
    TalentRouteSnapshot createTalentRoute(TalentRouteEvent event);
    TalentRouteSnapshot updateTalentRoute(TalentRouteSnapshot existingRoute, TalentRouteEvent event);
    void smartUpdateRouteTrackMappings(TalentRouteSnapshot route, List<Map<UUID, Integer>> growthTrackMappings);
    Optional<TalentRouteSnapshot> findByTalentRouteId(UUID talentRouteId);
    boolean existsByTalentRouteId(UUID talentRouteId);
    Optional<TalentRouteSnapshot> findByTalentRouteIdWithTrackMappings(UUID talentRouteId);

    // ===== OLD ENTITY-BASED METHODS (TO BE DEPRECATED) =====
    PaginatedResponseDto<TalentRouteSnapshot> findAll(Pageable pageable);
    PaginatedResponseDto<TalentRouteSnapshot> findAllWithTrackMappings(Pageable pageable);
    PaginatedResponseDto<TalentRouteSnapshot> searchByRouteName(String routeName, Pageable pageable);

    // ===== NEW CLEAN DTO-BASED METHODS =====
    PaginatedResponseDto<TalentRouteResponseDto> findAllBasic(Pageable pageable);
    PaginatedResponseDto<TalentRouteResponseDto> findAllWithTrackSummaries(Pageable pageable);
    PaginatedResponseDto<TalentRouteResponseDto> searchByRouteNameBasic(String routeName, Pageable pageable);
    Optional<TalentRouteResponseDto> findByTalentRouteIdBasic(UUID talentRouteId);
    Optional<TalentRouteResponseDto> findByTalentRouteIdWithTrackSummaries(UUID talentRouteId);
}