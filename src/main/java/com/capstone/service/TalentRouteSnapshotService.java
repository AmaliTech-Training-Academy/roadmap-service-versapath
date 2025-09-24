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
    TalentRouteSnapshot processTalentRouteEvent(TalentRouteEvent event);
    TalentRouteSnapshot createTalentRoute(TalentRouteEvent event);
    TalentRouteSnapshot updateTalentRoute(TalentRouteSnapshot existingRoute, TalentRouteEvent event);
    void smartUpdateRouteTrackMappings(TalentRouteSnapshot route, List<Map<UUID, Integer>> growthTrackMappings);
    TalentRouteSnapshot assignTracksToRoute(TalentRouteEvent event);


    PaginatedResponseDto<TalentRouteResponseDto> findAllBasic(Pageable pageable);
    PaginatedResponseDto<TalentRouteResponseDto> findAllWithTrackSummaries(Pageable pageable);
    PaginatedResponseDto<TalentRouteResponseDto> searchByRouteNameBasic(String routeName, Pageable pageable);
    Optional<TalentRouteResponseDto> findByTalentRouteIdBasic(UUID talentRouteId);
    Optional<TalentRouteResponseDto> findByTalentRouteIdWithTrackSummaries(UUID talentRouteId);
}