package com.capstone.repository;

import com.capstone.model.GrowthTrackSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GrowthTrackSnapshotRepository extends JpaRepository<GrowthTrackSnapshot, UUID> {

    Optional<GrowthTrackSnapshot> findByGrowthTrackId(UUID growthTrackId);

    boolean existsByGrowthTrackId(UUID growthTrackId);

    @Query("SELECT DISTINCT gt FROM GrowthTrackSnapshot gt " +
            "LEFT JOIN FETCH gt.trackCapsuleMappings tcm " +
            "LEFT JOIN FETCH tcm.skillCapsule " +
            "WHERE gt.growthTrackId = :growthTrackId " +
            "ORDER BY tcm.sequenceOrder ASC")
    Optional<GrowthTrackSnapshot> findByGrowthTrackIdWithCapsuleMappings(@Param("growthTrackId") UUID growthTrackId);

}
