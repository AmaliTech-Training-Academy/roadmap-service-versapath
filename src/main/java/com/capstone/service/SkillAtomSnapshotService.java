package com.capstone.service;

import com.capstone.model.SkillAtomSnapshot;
import org.common.event.SkillAtomEvent;

import java.util.Optional;
import java.util.UUID;

public interface SkillAtomSnapshotService {
    SkillAtomSnapshot processSkillAtomEvent(SkillAtomEvent event);
    SkillAtomSnapshot createSkillAtom(SkillAtomEvent event);
    SkillAtomSnapshot updateSkillAtom(SkillAtomSnapshot existingSkillAtom, SkillAtomEvent event);
    Optional<SkillAtomSnapshot> findBySkillAtomId(UUID skillAtomId);
    boolean existsBySkillAtomId(UUID skillAtomId);
}
