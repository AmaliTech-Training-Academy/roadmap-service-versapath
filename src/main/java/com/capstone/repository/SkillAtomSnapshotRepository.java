package com.capstone.repository;


import com.capstone.model.SkillAtomSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SkillAtomSnapshotRepository extends JpaRepository<SkillAtomSnapshot, UUID> {
    Optional<SkillAtomSnapshot> findBySkillAtomId(UUID skillAtomId);
    boolean existsBySkillAtomId(UUID skillAtomId);
}
