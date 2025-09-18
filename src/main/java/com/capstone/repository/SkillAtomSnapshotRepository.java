package com.capstone.repository;

import com.capstone.model.SkillAtomSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SkillAtomSnapshotRepository extends JpaRepository<SkillAtomSnapshot, UUID> {

    // ===== EXISTING METHODS =====
    Optional<SkillAtomSnapshot> findBySkillAtomId(UUID skillAtomId);
    boolean existsBySkillAtomId(UUID skillAtomId);

    // ===== NEW PAGINATION METHODS =====
    @Query("SELECT a FROM SkillAtomSnapshot a " +
            "WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<SkillAtomSnapshot> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    @Query("SELECT DISTINCT a FROM SkillAtomSnapshot a " +
            "INNER JOIN CapsuleAtomMapping cam ON cam.skillAtom.id = a.id " +
            "WHERE cam.skillCapsule.skillCapsuleId = :skillCapsuleId " +
            "ORDER BY cam.sequenceOrder ASC")
    Page<SkillAtomSnapshot> findBySkillCapsuleId(@Param("skillCapsuleId") UUID skillCapsuleId, Pageable pageable);
}
