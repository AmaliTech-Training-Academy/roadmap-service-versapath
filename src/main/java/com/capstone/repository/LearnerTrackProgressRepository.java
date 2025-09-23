package com.capstone.repository;

import com.capstone.model.LearnerTrackProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LearnerTrackProgressRepository extends JpaRepository<LearnerTrackProgress, UUID> {

    @Query("""
        SELECT lcp, tcm.sequenceOrder FROM LearnerCapsuleProgress lcp
        JOIN lcp.learnerTrackProgress ltp
        JOIN ltp.learnerRoadmap lr
        JOIN TrackCapsuleMapping tcm ON tcm.skillCapsule.id = lcp.skillCapsule.id
            AND tcm.growthTrack.id = ltp.growthTrack.id
        WHERE lr.userId = :learnerId
        AND ltp.growthTrack.growthTrackId = :trackId
        AND lr.enrollmentStatus = 'ACTIVE'
        ORDER BY tcm.sequenceOrder
    """)
    List<Object[]> findCapsuleProgressesByLearnerIdAndTrackIdWithSequence(
        @Param("learnerId") UUID learnerId,
        @Param("trackId") UUID trackId);

    @Query("""
        SELECT ltp, rtm.sequenceOrder FROM LearnerTrackProgress ltp
        JOIN ltp.learnerRoadmap lr
        JOIN RouteTrackMapping rtm ON rtm.growthTrack.id = ltp.growthTrack.id
            AND rtm.talentRoute.id = lr.talentRoute.id
        WHERE lr.userId = :learnerId
        AND lr.enrollmentStatus = 'ACTIVE'
        ORDER BY rtm.sequenceOrder
    """)
    List<Object[]> findTrackProgressesByLearnerIdWithSequence(@Param("learnerId") UUID learnerId);
}
