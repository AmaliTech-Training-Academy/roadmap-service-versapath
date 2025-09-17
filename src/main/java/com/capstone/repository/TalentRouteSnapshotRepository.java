package com.capstone.repository;

import com.capstone.model.TalentRouteSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TalentRouteSnapshotRepository extends JpaRepository<TalentRouteSnapshot, UUID> {

    Optional<TalentRouteSnapshot> findByTalentRouteId(UUID talentRouteId);

    boolean existsByTalentRouteId(UUID talentRouteId);

    @Query("SELECT DISTINCT tr FROM TalentRouteSnapshot tr " +
            "LEFT JOIN FETCH tr.routeTrackMappings rtm " +
            "LEFT JOIN FETCH rtm.growthTrack " +
            "WHERE tr.talentRouteId = :talentRouteId " +
            "ORDER BY rtm.sequenceOrder ASC")
    Optional<TalentRouteSnapshot> findByTalentRouteIdWithTrackMappings(@Param("talentRouteId") UUID talentRouteId);
}
