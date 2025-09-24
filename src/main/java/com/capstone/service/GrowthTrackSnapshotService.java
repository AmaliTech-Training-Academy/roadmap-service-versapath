package com.capstone.service;

import com.capstone.dto.response.GrowthTrackResponseDto;
import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.model.GrowthTrackSnapshot;
import org.common.event.GrowthTrackEvent;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface GrowthTrackSnapshotService {
    GrowthTrackSnapshot processGrowthTrackEvent(GrowthTrackEvent event);
    GrowthTrackSnapshot createGrowthTrack(GrowthTrackEvent event);
    GrowthTrackSnapshot updateGrowthTrack(GrowthTrackSnapshot existingTrack, GrowthTrackEvent event);
    void smartUpdateTrackCapsuleMappings(GrowthTrackSnapshot track, List<Map<UUID, Integer>> skillCapsuleMappings);
    Optional<GrowthTrackSnapshot> findByGrowthTrackId(UUID growthTrackId);
    GrowthTrackSnapshot  assignCapsulesToTrack(GrowthTrackEvent event);


    PaginatedResponseDto<GrowthTrackResponseDto> findAllBasic(Pageable pageable);
    PaginatedResponseDto<GrowthTrackResponseDto> findAllWithCapsuleSummaries(Pageable pageable);
    PaginatedResponseDto<GrowthTrackResponseDto> searchByTrackNameBasic(String trackName, Pageable pageable);
    Optional<GrowthTrackResponseDto> findByGrowthTrackIdBasic(UUID growthTrackId);
    Optional<GrowthTrackResponseDto> findByGrowthTrackIdWithCapsuleSummaries(UUID growthTrackId);
}
