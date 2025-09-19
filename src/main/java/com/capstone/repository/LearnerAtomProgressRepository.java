package com.capstone.repository;

import com.capstone.model.LearnerAtomProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearnerAtomProgressRepository extends JpaRepository<LearnerAtomProgress, UUID> {

    @Query("""
        SELECT lap
        FROM LearnerAtomProgress lap
        JOIN lap.learnerCapsuleProgress lcp
        JOIN lcp.learnerTrackProgress ltp
        JOIN ltp.learnerRoadmap lr
        WHERE lr.userId = :learnerId
          AND ltp.growthTrack.growthTrackId = :trackId
          AND lap.skillAtom.skillAtomId = :atomId
    """)
    Optional<LearnerAtomProgress> findByLearnerIdAndAtomId(@Param("learnerId") UUID learnerId,
                                                           @Param("atomId") UUID atomId,
                                                           @Param("trackId") UUID trackId);

}
