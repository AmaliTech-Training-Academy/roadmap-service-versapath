package com.capstone.service;

import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.dto.response.TalentRouteResponseDto;
import com.capstone.model.TalentRouteSnapshot;
import org.common.event.TalentRouteEvent;
import org.springframework.data.domain.Pageable;

import java.util.*;

public interface TalentRouteSnapshotService {
    TalentRouteSnapshot processTalentRouteEvent(TalentRouteEvent event);
    TalentRouteSnapshot createTalentRoute(TalentRouteEvent event);
    TalentRouteSnapshot updateTalentRoute(TalentRouteSnapshot existingRoute, TalentRouteEvent event);
    void smartUpdateRouteTrackMappings(TalentRouteSnapshot route, List<Map<UUID, Integer>> growthTrackMappings);
    TalentRouteSnapshot assignTracksToRoute(TalentRouteEvent event);
    Optional<TalentRouteSnapshot> findByTalentRouteId(UUID talentRouteId);
    List<TalentRouteSnapshot> findByTalentRouteIds(Collection<UUID> talentRouteIds);


    PaginatedResponseDto<TalentRouteResponseDto> findAllBasic(Pageable pageable);
    PaginatedResponseDto<TalentRouteResponseDto> findAllWithTrackSummaries(Pageable pageable);
    PaginatedResponseDto<TalentRouteResponseDto> searchByRouteNameBasic(String routeName, Pageable pageable);
    Optional<TalentRouteResponseDto> findByTalentRouteIdBasic(UUID talentRouteId);
    Optional<TalentRouteResponseDto> findByTalentRouteIdWithTrackSummaries(UUID talentRouteId);
}