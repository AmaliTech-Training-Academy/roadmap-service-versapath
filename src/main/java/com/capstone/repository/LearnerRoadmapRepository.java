package com.capstone.repository;

import com.capstone.model.LearnerRoadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    @Query("""
        SELECT DISTINCT rm FROM LearnerRoadmap rm
        LEFT JOIN FETCH rm.talentRoute tr
        WHERE rm.userId = :learnerId
        AND rm.enrollmentStatus = 'ACTIVE'
    """)
    Optional<LearnerRoadmap> findActiveRoadmapByUserId(@Param("learnerId") UUID learnerId);

    @Query("""
        SELECT rm FROM LearnerRoadmap rm
        WHERE rm.userId = :userId
        AND rm.talentRoute.talentRouteId != :talentRouteId
    """)
    List<LearnerRoadmap> findLearnerRoadmapByUserIdANDTalentRouteId(@Param("userId") UUID userId,
                                                                    @Param("talentRouteId") UUID talentRouteId);

    @Query("""
         SELECT lr.userId, lr.talentRoute.talentRouteId
         FROM LearnerRoadmap lr
         JOIN lr.learnerTrackProgresses ltp
         WHERE ltp.growthTrack.growthTrackId = :growthTrackId
         AND lr.enrollmentStatus = 'ACTIVE'
     """)
    List<Object[]> findActiveLearnersByGrowthTrackId(@Param("growthTrackId") UUID growthTrackId);

    @Query("""
         SELECT lr.userId, lr.talentRoute.talentRouteId
         FROM LearnerRoadmap lr
         JOIN lr.learnerTrackProgresses ltp
         JOIN ltp.learnerCapsuleProgresses lcp
         WHERE lcp.skillCapsule.skillCapsuleId = :capsuleId
         AND lr.enrollmentStatus = 'ACTIVE'
     """)
    List<Object[]> findActiveLearnersByCapsuleId(@Param("capsuleId") UUID capsuleId);
}
