package com.capstone.repository;

import com.capstone.model.LearnerRoadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearnerRoadmapRepository extends JpaRepository<LearnerRoadmap, UUID> {

    @Query("""
        SELECT rm FROM LearnerRoadmap rm
        WHERE rm.userId = :userId
        AND rm.talentRoute.talentRouteId = :talentRouteId
    """)
    Optional<LearnerRoadmap> findByUserIdAndTalentRouteId(@Param("userId") UUID userId,
                                                          @Param("talentRouteId") UUID talentRouteId);

}
