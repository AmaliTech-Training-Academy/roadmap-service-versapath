package com.capstone.repository;

import com.capstone.model.SkillCapsuleSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SkillCapsuleSnapshotRepository extends JpaRepository<SkillCapsuleSnapshot, UUID> {
}
