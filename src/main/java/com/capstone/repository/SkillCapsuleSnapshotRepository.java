package com.capstone.repository;

import com.capstone.model.SkillCapsuleSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SkillCapsuleSnapshotRepository extends JpaRepository<SkillCapsuleSnapshot, UUID> {

    // ===== EXISTING METHODS =====
    Optional<SkillCapsuleSnapshot> findBySkillCapsuleId(UUID skillCapsuleId);
    boolean existsBySkillCapsuleId(UUID skillCapsuleId);

    @Query("SELECT DISTINCT c FROM SkillCapsuleSnapshot c " +
            "LEFT JOIN FETCH c.capsuleAtomMappings cam " +
            "LEFT JOIN FETCH cam.skillAtom " +
            "WHERE c.skillCapsuleId = :skillCapsuleId " +
            "ORDER BY cam.sequenceOrder ASC")
    Optional<SkillCapsuleSnapshot> findBySkillCapsuleIdWithAtomMappings(@Param("skillCapsuleId") UUID skillCapsuleId);

    // ===== NEW PAGINATION METHODS =====
    @Query("SELECT DISTINCT c FROM SkillCapsuleSnapshot c " +
            "LEFT JOIN FETCH c.capsuleAtomMappings cam " +
            "LEFT JOIN FETCH cam.skillAtom")
    Page<SkillCapsuleSnapshot> findAllWithAtomMappings(Pageable pageable);

    @Query("SELECT c FROM SkillCapsuleSnapshot c " +
            "WHERE LOWER(c.capsuleName) LIKE LOWER(CONCAT('%', :capsuleName, '%'))")
    Page<SkillCapsuleSnapshot> findByCapsuleNameContainingIgnoreCase(@Param("capsuleName") String capsuleName, Pageable pageable);

    @Query("SELECT c FROM SkillCapsuleSnapshot c " +
            "WHERE LOWER(c.difficultyLevel) = LOWER(:difficultyLevel)")
    Page<SkillCapsuleSnapshot> findByDifficultyLevel(@Param("difficultyLevel") String difficultyLevel, Pageable pageable);

    @Query("SELECT DISTINCT c FROM SkillCapsuleSnapshot c " +
            "INNER JOIN TrackCapsuleMapping tcm ON tcm.skillCapsule.id = c.id " +
            "WHERE tcm.growthTrack.growthTrackId = :growthTrackId " +
            "ORDER BY tcm.sequenceOrder ASC")
    Page<SkillCapsuleSnapshot> findByGrowthTrackId(@Param("growthTrackId") UUID growthTrackId, Pageable pageable);
}
