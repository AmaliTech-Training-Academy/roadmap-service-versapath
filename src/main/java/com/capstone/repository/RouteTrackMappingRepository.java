package com.capstone.repository;

import com.capstone.model.GrowthTrackSnapshot;
import com.capstone.model.RouteTrackMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RouteTrackMappingRepository extends JpaRepository<RouteTrackMapping, UUID> {
   // find all the growth tracks that belong to a talent route
    @Query("""
    SELECT rtm.growthTrack
    FROM RouteTrackMapping rtm
    WHERE rtm.talentRoute.talentRouteId = :talentRouteId
    """)
    List<GrowthTrackSnapshot> findGrowthTracksByTalentRouteId(@Param("talentRouteId") UUID talentRouteId);

}
