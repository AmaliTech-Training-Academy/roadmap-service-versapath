package com.capstone.controller;

import com.capstone.dto.response.ApiResponseDto;
import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.dto.response.SkillAtomResponseDto;
import com.capstone.service.SkillAtomSnapshotService;
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
@RequestMapping("/api/v1/roadmap/skill-atoms")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Skill Atom Management", description = "APIs for managing skill atoms")
public class SkillAtomController {

    private final SkillAtomSnapshotService skillAtomService;

    @GetMapping
    @Operation(summary = "Get all skill atoms (basic info)", description = "Retrieve paginated list of skill atoms with basic information")
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<SkillAtomResponseDto>>> findAllBasic(
            @PageableDefault() Pageable pageable) {
        log.info("Finding all skill atoms (basic), page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        PaginatedResponseDto<SkillAtomResponseDto> result = skillAtomService.findAllBasic(pageable);
        return ResponseEntity.ok(ApiResponseDto.success(result, "Skill atoms retrieved successfully"));
    }

    @GetMapping("/search")
    @Operation(summary = "Search skill atoms by name", description = "Search for skill atoms by atom name (case-insensitive)")
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<SkillAtomResponseDto>>> searchByName(
            @Parameter(description = "Atom name to search for", required = true) @RequestParam String name,
            @PageableDefault() Pageable pageable) {
        log.info("Searching skill atoms by name: '{}', page: {}, size: {}", name, pageable.getPageNumber(), pageable.getPageSize());

        PaginatedResponseDto<SkillAtomResponseDto> result = skillAtomService.searchByNameBasic(name, pageable);
        return ResponseEntity.ok(ApiResponseDto.success(result, "Skill atoms search completed successfully"));
    }

    @GetMapping("/{skillAtomId}")
    @Operation(summary = "Get skill atom by ID (basic info)", description = "Retrieve a specific skill atom by its ID with basic information")
    public ResponseEntity<ApiResponseDto<SkillAtomResponseDto>> findBySkillAtomIdBasic(
            @Parameter(description = "Skill atom ID", required = true) @PathVariable UUID skillAtomId) {
        log.info("Finding skill atom by ID (basic): {}", skillAtomId);

        Optional<SkillAtomResponseDto> result = skillAtomService.findBySkillAtomIdBasic(skillAtomId);

        return result.map(skillAtomResponseDto -> ResponseEntity.ok(ApiResponseDto.success(skillAtomResponseDto, "Skill atom retrieved successfully"))).orElseGet(() -> ResponseEntity.notFound().build());
    }
}