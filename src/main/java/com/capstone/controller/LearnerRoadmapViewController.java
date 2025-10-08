package com.capstone.controller;

import com.capstone.dto.response.ApiResponseDto;
import com.capstone.dto.response.LearnerAtomProgressDto;
import com.capstone.dto.response.LearnerRoadmapWithProgressDto;
import com.capstone.dto.response.LearnerTrackProgressDto;
import com.capstone.service.LearnerRoadmapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/learner")
@RequiredArgsConstructor
@Tag(name = "Learner Roadmap View", description = "Learner-specific roadmap with progress")
public class LearnerRoadmapViewController {

    private final LearnerRoadmapService learnerRoadmapService;

    @GetMapping("/my-roadmap")
    @Operation(summary = "Get my roadmap with progress",
               description = "Get learner's enrolled roadmap with basic progress info")
    public ResponseEntity<ApiResponseDto<LearnerRoadmapWithProgressDto>> getMyRoadmap(
            @RequestHeader("X-User-ID") UUID learnerId) {

        Optional<LearnerRoadmapWithProgressDto> roadmap =
            learnerRoadmapService.getLearnerRoadmap(learnerId);

        return roadmap
            .map(r -> ResponseEntity.ok(ApiResponseDto.success(r, "Your roadmap retrieved successfully")))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/my-tracks/{learnerId}")
    @Operation(summary = "Get my tracks with progress and capsules",
            description = "Get all tracks for learner's roadmap with progress and associated capsules")
    public ResponseEntity<ApiResponseDto<List<LearnerTrackProgressDto>>> getMyTracks(
            @PathVariable UUID learnerId) {

        List<LearnerTrackProgressDto> tracks =
                learnerRoadmapService.getLearnerTracks(learnerId);

        return ResponseEntity.ok(ApiResponseDto.success(tracks, "Your tracks with capsules retrieved successfully"));
    }

    @GetMapping("/my-capsules/{capsuleId}/atoms")
    @Operation(summary = "Get capsule atoms with progress",
               description = "Get atoms for specific capsule with learner's progress")
    public ResponseEntity<ApiResponseDto<List<LearnerAtomProgressDto>>> getCapsuleAtoms(
            @PathVariable UUID capsuleId,
            @RequestHeader("X-User-ID") UUID learnerId) {

        List<LearnerAtomProgressDto> atoms =
            learnerRoadmapService.getCapsuleAtoms(learnerId, capsuleId);

        return ResponseEntity.ok(ApiResponseDto.success(atoms, "Capsule atoms retrieved successfully"));
    }
}