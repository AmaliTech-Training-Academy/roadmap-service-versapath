package com.capstone.controller;

import com.capstone.dto.response.ApiResponseDto;
import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.dto.response.SkillCapsuleResponseDto;
import com.capstone.service.SkillCapsuleSnapshotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roadmap/skill-capsules")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Skill Capsule Management", description = "APIs for managing skill capsules")
public class SkillCapsuleController {

    private final SkillCapsuleSnapshotService skillCapsuleService;

    @GetMapping
    @Operation(summary = "Get all skill capsules (basic info)", description = "Retrieve paginated list of skill capsules with basic information")
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<SkillCapsuleResponseDto>>> findAllBasic(
            @PageableDefault() Pageable pageable) {
        log.info("Finding all skill capsules (basic), page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        PaginatedResponseDto<SkillCapsuleResponseDto> result = skillCapsuleService.findAllBasic(pageable);
        return ResponseEntity.ok(ApiResponseDto.success(result, "Skill capsules retrieved successfully"));
    }

    @GetMapping("/with-atoms")
    @Operation(summary = "Get all skill capsules with atom summaries", description = "Retrieve paginated list of skill capsules with their skill atom summaries")
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<SkillCapsuleResponseDto>>> findAllWithAtomSummaries(
            @PageableDefault() Pageable pageable) {
        log.info("Finding all skill capsules with atom summaries, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        PaginatedResponseDto<SkillCapsuleResponseDto> result = skillCapsuleService.findAllWithAtomSummaries(pageable);
        return ResponseEntity.ok(ApiResponseDto.success(result, "Skill capsules with atom summaries retrieved successfully"));
    }

    @GetMapping("/search")
    @Operation(summary = "Search skill capsules by name", description = "Search for skill capsules by capsule name (case-insensitive)")
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<SkillCapsuleResponseDto>>> searchByCapsuleName(
            @Parameter(description = "Capsule name to search for", required = true) @RequestParam String name,
            @PageableDefault() Pageable pageable) {
        log.info("Searching skill capsules by name: '{}', page: {}, size: {}", name, pageable.getPageNumber(), pageable.getPageSize());

        PaginatedResponseDto<SkillCapsuleResponseDto> result = skillCapsuleService.searchByCapsuleNameBasic(name, pageable);
        return ResponseEntity.ok(ApiResponseDto.success(result, "Skill capsules search completed successfully"));
    }

    @GetMapping("/by-difficulty/{difficultyLevel}")
    @Operation(summary = "Get skill capsules by difficulty level", description = "Retrieve paginated list of skill capsules by difficulty level")
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<SkillCapsuleResponseDto>>> findByDifficultyLevel(
            @Parameter(description = "Difficulty level", required = true) @PathVariable String difficultyLevel,
            @PageableDefault() Pageable pageable) {
        log.info("Finding skill capsules by difficulty level: '{}', page: {}, size: {}", difficultyLevel, pageable.getPageNumber(), pageable.getPageSize());

        PaginatedResponseDto<SkillCapsuleResponseDto> result = skillCapsuleService.findByDifficultyLevelBasic(difficultyLevel, pageable);
        return ResponseEntity.ok(ApiResponseDto.success(result, "Skill capsules by difficulty level retrieved successfully"));
    }

    @GetMapping("/{skillCapsuleId}")
    @Operation(summary = "Get skill capsule by ID (basic info)", description = "Retrieve a specific skill capsule by its ID with basic information")
    public ResponseEntity<ApiResponseDto<SkillCapsuleResponseDto>> findBySkillCapsuleIdBasic(
            @Parameter(description = "Skill capsule ID", required = true) @PathVariable UUID skillCapsuleId) {
        log.info("Finding skill capsule by ID (basic): {}", skillCapsuleId);

        Optional<SkillCapsuleResponseDto> result = skillCapsuleService.findBySkillCapsuleIdBasic(skillCapsuleId);

        return result.map(skillCapsuleResponseDto -> ResponseEntity.ok(ApiResponseDto.success(skillCapsuleResponseDto, "Skill capsule retrieved successfully"))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{skillCapsuleId}/with-atoms")
    @Operation(summary = "Get skill capsule by ID with atom summaries", description = "Retrieve a specific skill capsule by its ID with skill atom summaries")
    public ResponseEntity<ApiResponseDto<SkillCapsuleResponseDto>> findBySkillCapsuleIdWithAtomSummaries(
            @Parameter(description = "Skill capsule ID", required = true) @PathVariable UUID skillCapsuleId) {
        log.info("Finding skill capsule by ID with atom summaries: {}", skillCapsuleId);

        Optional<SkillCapsuleResponseDto> result = skillCapsuleService.findBySkillCapsuleIdWithAtomSummaries(skillCapsuleId);

        return result.map(skillCapsuleResponseDto -> ResponseEntity.ok(ApiResponseDto.success(skillCapsuleResponseDto, "Skill capsule with atom summaries retrieved successfully"))).orElseGet(() -> ResponseEntity.notFound().build());
    }
}