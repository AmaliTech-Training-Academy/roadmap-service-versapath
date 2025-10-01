package com.capstone.controller;

import com.capstone.dto.response.ApiResponseDto;
import com.capstone.dto.response.LearnerDto;
import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.dto.response.MentorResponseDto;
import com.capstone.service.MentorSnapshotService;
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
@RequestMapping("/api/v1/roadmap/mentors")
@RequiredArgsConstructor
@Slf4j
public class MentorController {

    private final MentorSnapshotService mentorSnapshotService;

    /**
     * Get all mentors (basic info only)
     */
    @GetMapping
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<MentorResponseDto>>> getAllMentors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        log.info("Fetching mentors: page={}, size={}, sort={}, direction={}",
                page, size, sort, direction);

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PaginatedResponseDto<MentorResponseDto> result = mentorSnapshotService.findAllBasic(pageable);
        return ResponseEntity.ok(ApiResponseDto.success(result, "Mentors retrieved successfully"));
    }

    /**
     * Get all mentors with their specializations
     */
    @GetMapping("/with-specializations")
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<MentorResponseDto>>> getAllMentorsWithSpecializations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        log.info("Fetching mentors with specializations: page={}, size={}, sort={}, direction={}",
                page, size, sort, direction);

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PaginatedResponseDto<MentorResponseDto> result = mentorSnapshotService.findAllWithSpecializations(pageable);
        return ResponseEntity.ok(ApiResponseDto.success(result, "Mentors with specializations retrieved successfully"));
    }

    /**
     * Search mentors by name
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<MentorResponseDto>>> searchMentors(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        log.info("Searching mentors by name '{}': page={}, size={}, sort={}, direction={}",
                name, page, size, sort, direction);

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PaginatedResponseDto<MentorResponseDto> result = mentorSnapshotService.searchByNameBasic(name, pageable);
        return ResponseEntity.ok(ApiResponseDto.success(result, "Search results retrieved successfully"));
    }

    /**
     * Get single mentor by ID (basic info only)
     */
    @GetMapping("/{mentorId}")
    public ResponseEntity<ApiResponseDto<MentorResponseDto>> getMentorById(@PathVariable UUID mentorId) {
        log.info("Fetching mentor by ID: {}", mentorId);

        Optional<MentorResponseDto> mentor = mentorSnapshotService.findByMentorIdBasic(mentorId);
        return mentor.map(m -> ResponseEntity.ok(ApiResponseDto.success(m, "Mentor retrieved successfully")))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get single mentor with their specializations
     */
    @GetMapping("/{mentorId}/with-specializations")
    public ResponseEntity<ApiResponseDto<MentorResponseDto>> getMentorByIdWithSpecializations(@PathVariable UUID mentorId) {
        log.info("Fetching mentor with specializations by ID: {}", mentorId);

        Optional<MentorResponseDto> mentor = mentorSnapshotService.findByMentorIdWithSpecializations(mentorId);
        return mentor.map(m -> ResponseEntity.ok(ApiResponseDto.success(m, "Mentor with specializations retrieved successfully")))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get mentors by specialization (talent route)
     */
    @GetMapping("/by-specialization/{talentRouteId}")
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<MentorResponseDto>>> getMentorsBySpecialization(
            @PathVariable UUID talentRouteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        log.info("Fetching mentors by specialization: {}, page={}, size={}, sort={}, direction={}",
                talentRouteId, page, size, sort, direction);

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PaginatedResponseDto<MentorResponseDto> result = mentorSnapshotService.findBySpecialization(talentRouteId, pageable);
        return ResponseEntity.ok(ApiResponseDto.success(result, "Mentors by specialization retrieved successfully"));
    }

    @GetMapping("/assigned-learners/{mentorId}")
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<LearnerDto>>> getAssignedLearners(
            @PathVariable UUID mentorId, Pageable pageable) {

        PaginatedResponseDto<LearnerDto> result = mentorSnapshotService.findLearnersByMentorId(mentorId, pageable);
        return ResponseEntity.ok(ApiResponseDto.success(result, "Assigned learners retrieved successfully"));
    }
}
