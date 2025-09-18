package com.capstone.repository;

import com.capstone.model.GrowthTrackSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GrowthTrackSnapshotRepository extends JpaRepository<GrowthTrackSnapshot, UUID> {

    // ===== EXISTING METHODS =====
    Optional<GrowthTrackSnapshot> findByGrowthTrackId(UUID growthTrackId);

    boolean existsByGrowthTrackId(UUID growthTrackId);

    @Query("SELECT DISTINCT gt FROM GrowthTrackSnapshot gt " +
            "LEFT JOIN FETCH gt.trackCapsuleMappings tcm " +
            "LEFT JOIN FETCH tcm.skillCapsule " +
            "WHERE gt.growthTrackId = :growthTrackId " +
            "ORDER BY tcm.sequenceOrder ASC")
    Optional<GrowthTrackSnapshot> findByGrowthTrackIdWithCapsuleMappings(@Param("growthTrackId") UUID growthTrackId);

    // ===== NEW PAGINATION METHODS =====
    @Query("SELECT DISTINCT gt FROM GrowthTrackSnapshot gt " +
            "LEFT JOIN FETCH gt.trackCapsuleMappings tcm " +
            "LEFT JOIN FETCH tcm.skillCapsule")
    Page<GrowthTrackSnapshot> findAllWithCapsuleMappings(Pageable pageable);

    @Query("SELECT gt FROM GrowthTrackSnapshot gt " +
            "WHERE LOWER(gt.trackName) LIKE LOWER(CONCAT('%', :trackName, '%'))")
    Page<GrowthTrackSnapshot> findByTrackNameContainingIgnoreCase(@Param("trackName") String trackName, Pageable pageable);

    @Query("SELECT DISTINCT gt FROM GrowthTrackSnapshot gt " +
            "INNER JOIN RouteTrackMapping rtm ON rtm.growthTrack.id = gt.id " +
            "WHERE rtm.talentRoute.talentRouteId = :talentRouteId " +
            "ORDER BY rtm.sequenceOrder ASC")
    Page<GrowthTrackSnapshot> findByTalentRouteId(@Param("talentRouteId") UUID talentRouteId, Pageable pageable);
}
