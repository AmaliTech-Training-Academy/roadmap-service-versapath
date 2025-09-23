package com.capstone.repository;

import com.capstone.model.LearnerCapsuleProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LearnerCapsuleProgressRepository extends JpaRepository<LearnerCapsuleProgress, UUID> {

    @Query("""
        SELECT lap, cam.sequenceOrder FROM LearnerAtomProgress lap
        JOIN lap.learnerCapsuleProgress lcp
        JOIN lcp.learnerTrackProgress ltp
        JOIN ltp.learnerRoadmap lr
        JOIN CapsuleAtomMapping cam ON cam.skillAtom.id = lap.skillAtom.id
            AND cam.skillCapsule.id = lcp.skillCapsule.id
        WHERE lr.userId = :learnerId
        AND lcp.skillCapsule.skillCapsuleId = :capsuleId
        AND lr.enrollmentStatus = 'ACTIVE'
        ORDER BY cam.sequenceOrder
    """)
    List<Object[]> findAtomProgressesByLearnerIdAndCapsuleIdWithSequence(
        @Param("learnerId") UUID learnerId,
        @Param("capsuleId") UUID capsuleId);
}
