package com.capstone.controller;

import com.capstone.dto.response.ApiResponseDto;
import com.capstone.dto.request.RecalculateProgressRequestDto;
import com.capstone.dto.request.AtomProgressRequestDto;
import com.capstone.service.LearnerProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/roadmap")
@Tag(name = "Roadmap Controller", description = "Manage all roadmap's api")
public class RoadmapController {
    private final LearnerProgressService learnerProgressService;

    @PostMapping("/start-progress")
    @Operation(summary = "Start progress", description = "This end point gets a learner to start tracking progress")
    public ResponseEntity<ApiResponseDto<String>> startTrackingProgress(@RequestBody AtomProgressRequestDto dto) {
        learnerProgressService.startAtomProgress(dto);
        return ResponseEntity.ok(ApiResponseDto.success("You've started your learning path"));
    }

    @PostMapping("/complete-atom-progress")
    @Operation(summary = "Update progress", description = "This end point calculates learner's progress")
    public ResponseEntity<ApiResponseDto<String>> updateLearnerProgress(@RequestBody AtomProgressRequestDto dto) {
        learnerProgressService.completeAtomProgress(dto);
        return ResponseEntity.ok(ApiResponseDto.success("Congratulation on completing this lesson"));
    }

    @PostMapping("/recalculate-progress")
    @Operation(summary = "Recalculate", description = "This end point recalculates learner's progress for consistency")
    public ResponseEntity<ApiResponseDto<String>> recalculateLearnerProgress(@RequestBody RecalculateProgressRequestDto dto) {
        learnerProgressService.recalculateAndUpdateLearnerRoadmap(dto);
        return ResponseEntity.ok(ApiResponseDto.success("Async learner progress successfully"));
    }

}
