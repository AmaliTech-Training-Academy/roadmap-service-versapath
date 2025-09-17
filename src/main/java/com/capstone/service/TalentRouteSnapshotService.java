package com.capstone.service;

import com.capstone.model.TalentRouteSnapshot;
import org.common.event.TalentRouteEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface TalentRouteSnapshotService {
    TalentRouteSnapshot processTalentRouteEvent(TalentRouteEvent event);
    TalentRouteSnapshot createTalentRoute(TalentRouteEvent event);
    TalentRouteSnapshot updateTalentRoute(TalentRouteSnapshot existingRoute, TalentRouteEvent event);
    void smartUpdateRouteTrackMappings(TalentRouteSnapshot route, List<Map<UUID, Integer>> growthTrackMappings);
    Optional<TalentRouteSnapshot> findByTalentRouteId(UUID talentRouteId);
    boolean existsByTalentRouteId(UUID talentRouteId);
    Optional<TalentRouteSnapshot> findByTalentRouteIdWithTrackMappings(UUID talentRouteId);
}