package com.capstone.controller;

import com.capstone.dto.response.ApiResponseDto;
import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.dto.response.TalentRouteResponseDto;
import com.capstone.service.TalentRouteSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roadmap/talent-routes")
@RequiredArgsConstructor
@Slf4j
public class TalentRouteController {

    private final TalentRouteSnapshotService talentRouteSnapshotService;

    /**
     * Get all talent routes (basic info only)
     */
    @GetMapping
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<TalentRouteResponseDto>>> getAllTalentRoutes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "routeName") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        log.info("Fetching talent routes: page={}, size={}, sort={}, direction={}",
            page, size, sort, direction);

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
            Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PaginatedResponseDto<TalentRouteResponseDto> result = talentRouteSnapshotService.findAllBasic(pageable);
        return ResponseEntity.ok(ApiResponseDto.success(result, "Talent routes retrieved successfully"));
    }

    /**
     * Get all talent routes with their growth track summaries
     */
    @GetMapping("/with-tracks")
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<TalentRouteResponseDto>>> getAllTalentRoutesWithTracks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "routeName") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        log.info("Fetching talent routes with tracks: page={}, size={}, sort={}, direction={}",
            page, size, sort, direction);

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
            Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PaginatedResponseDto<TalentRouteResponseDto> result = talentRouteSnapshotService.findAllWithTrackSummaries(pageable);
        return ResponseEntity.ok(ApiResponseDto.success(result, "Talent routes with tracks retrieved successfully"));
    }

    /**
     * Search talent routes by name
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<TalentRouteResponseDto>>> searchTalentRoutes(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "routeName") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        log.info("Searching talent routes by name '{}': page={}, size={}, sort={}, direction={}",
            name, page, size, sort, direction);

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
            Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PaginatedResponseDto<TalentRouteResponseDto> result = talentRouteSnapshotService.searchByRouteNameBasic(name, pageable);
        return ResponseEntity.ok(ApiResponseDto.success(result, "Search results retrieved successfully"));
    }

    /**
     * Get single talent route by ID (basic info only)
     */
    @GetMapping("/{routeId}")
    public ResponseEntity<ApiResponseDto<TalentRouteResponseDto>> getTalentRouteById(@PathVariable UUID routeId) {
        log.info("Fetching talent route by ID: {}", routeId);

        Optional<TalentRouteResponseDto> route = talentRouteSnapshotService.findByTalentRouteIdBasic(routeId);
        return route.map(r -> ResponseEntity.ok(ApiResponseDto.success(r, "Talent route retrieved successfully")))
                   .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get single talent route with its growth track summaries
     */
    @GetMapping("/{routeId}/with-tracks")
    public ResponseEntity<ApiResponseDto<TalentRouteResponseDto>> getTalentRouteByIdWithTracks(@PathVariable UUID routeId) {
        log.info("Fetching talent route with tracks by ID: {}", routeId);

        Optional<TalentRouteResponseDto> route = talentRouteSnapshotService.findByTalentRouteIdWithTrackSummaries(routeId);
        return route.map(r -> ResponseEntity.ok(ApiResponseDto.success(r, "Talent route with tracks retrieved successfully")))
                   .orElse(ResponseEntity.notFound().build());
    }
}