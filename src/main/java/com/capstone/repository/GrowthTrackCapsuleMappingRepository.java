package com.capstone.repository;

import com.capstone.model.SkillCapsuleSnapshot;
import com.capstone.model.TrackCapsuleMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GrowthTrackCapsuleMappingRepository extends JpaRepository<TrackCapsuleMapping, UUID> {
   // find all the capsules that belong to a growth track
    @Query("""
    SELECT gtcm.skillCapsule
    FROM TrackCapsuleMapping gtcm
    WHERE gtcm.growthTrack.growthTrackId = :growthTrackId
    """)
    List<SkillCapsuleSnapshot> findCapsulesByGrowthTrackId(@Param("growthTrackId") UUID growthTrackId);
}
