package com.capstone.repository;

import com.capstone.model.SkillCapsuleSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SkillCapsuleSnapshotRepository extends JpaRepository<SkillCapsuleSnapshot, UUID> {

    Optional<SkillCapsuleSnapshot> findBySkillCapsuleId(UUID skillCapsuleId);
    boolean existsBySkillCapsuleId(UUID skillCapsuleId);

    @Query("SELECT DISTINCT c FROM SkillCapsuleSnapshot c " +
            "LEFT JOIN FETCH c.capsuleAtomMappings cam " +
            "LEFT JOIN FETCH cam.skillAtom " +
            "WHERE c.skillCapsuleId = :skillCapsuleId " +
            "ORDER BY cam.sequenceOrder ASC")
    Optional<SkillCapsuleSnapshot> findBySkillCapsuleIdWithAtomMappings(@Param("skillCapsuleId") UUID skillCapsuleId);
}
