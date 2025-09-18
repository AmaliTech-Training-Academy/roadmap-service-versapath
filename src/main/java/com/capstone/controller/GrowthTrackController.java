package com.capstone.controller;

import com.capstone.dto.response.ApiResponseDto;
import com.capstone.dto.response.GrowthTrackResponseDto;
import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.service.GrowthTrackSnapshotService;
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
@RequestMapping("/api/v1/roadmap/growth-tracks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Growth Track Management", description = "APIs for managing growth tracks")
public class GrowthTrackController {

    private final GrowthTrackSnapshotService growthTrackService;

    @GetMapping
    @Operation(summary = "Get all growth tracks (basic info)", description = "Retrieve paginated list of growth tracks with basic information")
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<GrowthTrackResponseDto>>> findAllBasic(
            @PageableDefault() Pageable pageable) {
        log.info("Finding all growth tracks (basic), page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        PaginatedResponseDto<GrowthTrackResponseDto> result = growthTrackService.findAllBasic(pageable);
        return ResponseEntity.ok(ApiResponseDto.success(result, "Growth tracks retrieved successfully"));
    }

    @GetMapping("/with-capsules")
    @Operation(summary = "Get all growth tracks with capsule summaries", description = "Retrieve paginated list of growth tracks with their skill capsule summaries")
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<GrowthTrackResponseDto>>> findAllWithCapsuleSummaries(
            @PageableDefault() Pageable pageable) {
        log.info("Finding all growth tracks with capsule summaries, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        PaginatedResponseDto<GrowthTrackResponseDto> result = growthTrackService.findAllWithCapsuleSummaries(pageable);
        return ResponseEntity.ok(ApiResponseDto.success(result, "Growth tracks with capsule summaries retrieved successfully"));
    }

    @GetMapping("/search")
    @Operation(summary = "Search growth tracks by name", description = "Search for growth tracks by track name (case-insensitive)")
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<GrowthTrackResponseDto>>> searchByTrackName(
            @Parameter(description = "Track name to search for", required = true) @RequestParam String name,
            @PageableDefault() Pageable pageable) {
        log.info("Searching growth tracks by name: '{}', page: {}, size: {}", name, pageable.getPageNumber(), pageable.getPageSize());

        PaginatedResponseDto<GrowthTrackResponseDto> result = growthTrackService.searchByTrackNameBasic(name, pageable);
        return ResponseEntity.ok(ApiResponseDto.success(result, "Growth tracks search completed successfully"));
    }

    @GetMapping("/{growthTrackId}")
    @Operation(summary = "Get growth track by ID (basic info)", description = "Retrieve a specific growth track by its ID with basic information")
    public ResponseEntity<ApiResponseDto<GrowthTrackResponseDto>> findByGrowthTrackIdBasic(
            @Parameter(description = "Growth track ID", required = true) @PathVariable UUID growthTrackId) {
        log.info("Finding growth track by ID (basic): {}", growthTrackId);

        Optional<GrowthTrackResponseDto> result = growthTrackService.findByGrowthTrackIdBasic(growthTrackId);

        return result.map(growthTrackResponseDto -> ResponseEntity.ok(ApiResponseDto.success(growthTrackResponseDto, "Growth track retrieved successfully"))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{growthTrackId}/with-capsules")
    @Operation(summary = "Get growth track by ID with capsule summaries", description = "Retrieve a specific growth track by its ID with skill capsule summaries")
    public ResponseEntity<ApiResponseDto<GrowthTrackResponseDto>> findByGrowthTrackIdWithCapsuleSummaries(
            @Parameter(description = "Growth track ID", required = true) @PathVariable UUID growthTrackId) {
        log.info("Finding growth track by ID with capsule summaries: {}", growthTrackId);

        Optional<GrowthTrackResponseDto> result = growthTrackService.findByGrowthTrackIdWithCapsuleSummaries(growthTrackId);

        return result.map(growthTrackResponseDto -> ResponseEntity.ok(ApiResponseDto.success(growthTrackResponseDto, "Growth track with capsule summaries retrieved successfully"))).orElseGet(() -> ResponseEntity.notFound().build());
    }
}