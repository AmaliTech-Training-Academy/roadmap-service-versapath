package com.capstone.service;

import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.dto.response.SkillAtomResponseDto;
import com.capstone.model.SkillAtomSnapshot;
import org.common.event.SkillAtomEvent;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface SkillAtomSnapshotService {

    // ===== EXISTING KAFKA/PROCESSING METHODS =====
    SkillAtomSnapshot processSkillAtomEvent(SkillAtomEvent event);
    SkillAtomSnapshot createSkillAtom(SkillAtomEvent event);
    SkillAtomSnapshot updateSkillAtom(SkillAtomSnapshot existingSkillAtom, SkillAtomEvent event);
    Optional<SkillAtomSnapshot> findBySkillAtomId(UUID skillAtomId);
    boolean existsBySkillAtomId(UUID skillAtomId);

    // ===== NEW CLEAN DTO-BASED METHODS =====
    PaginatedResponseDto<SkillAtomResponseDto> findAllBasic(Pageable pageable);
    PaginatedResponseDto<SkillAtomResponseDto> searchByNameBasic(String name, Pageable pageable);
    Optional<SkillAtomResponseDto> findBySkillAtomIdBasic(UUID skillAtomId);
}
