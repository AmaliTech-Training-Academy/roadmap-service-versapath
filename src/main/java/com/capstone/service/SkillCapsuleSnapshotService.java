package com.capstone.service;

import com.capstone.model.SkillCapsuleSnapshot;
import org.common.event.SkillCapsuleEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface SkillCapsuleSnapshotService {

    SkillCapsuleSnapshot processSkillCapsuleEvent(SkillCapsuleEvent event);
    SkillCapsuleSnapshot createSkillCapsule(SkillCapsuleEvent event);
    SkillCapsuleSnapshot updateSkillCapsule(SkillCapsuleSnapshot existingCapsule, SkillCapsuleEvent event);
    void smartUpdateCapsuleAtomMappings(SkillCapsuleSnapshot capsule, List<Map<UUID, Integer>> skillAtomMappings);
    Optional<SkillCapsuleSnapshot> findBySkillCapsuleId(UUID skillCapsuleId);
    boolean existsBySkillCapsuleId(UUID skillCapsuleId);
    Optional<SkillCapsuleSnapshot> findBySkillCapsuleIdWithAtomMappings(UUID skillCapsuleId);
}
