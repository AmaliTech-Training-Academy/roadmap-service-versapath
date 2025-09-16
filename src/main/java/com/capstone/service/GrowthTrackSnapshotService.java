package com.capstone.service;

import com.capstone.model.GrowthTrackSnapshot;
import org.common.event.GrowthTrackEvent;

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
    boolean existsByGrowthTrackId(UUID growthTrackId);
    Optional<GrowthTrackSnapshot> findByGrowthTrackIdWithCapsuleMappings(UUID growthTrackId);
}
