package com.capstone.service;

import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.dto.response.SkillCapsuleResponseDto;
import com.capstone.model.SkillCapsuleSnapshot;
import org.common.event.SkillCapsuleEvent;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface SkillCapsuleSnapshotService {

    // ===== EXISTING KAFKA/PROCESSING METHODS =====
    SkillCapsuleSnapshot processSkillCapsuleEvent(SkillCapsuleEvent event);
    SkillCapsuleSnapshot createSkillCapsule(SkillCapsuleEvent event);
    SkillCapsuleSnapshot updateSkillCapsule(SkillCapsuleSnapshot existingCapsule, SkillCapsuleEvent event);
    void smartUpdateCapsuleAtomMappings(SkillCapsuleSnapshot capsule, List<Map<UUID, Integer>> skillAtomMappings);
    Optional<SkillCapsuleSnapshot> findBySkillCapsuleId(UUID skillCapsuleId);
    boolean existsBySkillCapsuleId(UUID skillCapsuleId);
    Optional<SkillCapsuleSnapshot> findBySkillCapsuleIdWithAtomMappings(UUID skillCapsuleId);

    // ===== NEW CLEAN DTO-BASED METHODS =====
    PaginatedResponseDto<SkillCapsuleResponseDto> findAllBasic(Pageable pageable);
    PaginatedResponseDto<SkillCapsuleResponseDto> findAllWithAtomSummaries(Pageable pageable);
    PaginatedResponseDto<SkillCapsuleResponseDto> searchByCapsuleNameBasic(String capsuleName, Pageable pageable);
    PaginatedResponseDto<SkillCapsuleResponseDto> findByDifficultyLevelBasic(String difficultyLevel, Pageable pageable);
    Optional<SkillCapsuleResponseDto> findBySkillCapsuleIdBasic(UUID skillCapsuleId);
    Optional<SkillCapsuleResponseDto> findBySkillCapsuleIdWithAtomSummaries(UUID skillCapsuleId);
}
