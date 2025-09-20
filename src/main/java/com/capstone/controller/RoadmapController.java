package com.capstone.controller;

import com.capstone.dto.roadmap.RoadmapRequestDto;
import com.capstone.dto.roadmap.StartAtomProgressRequestDto;
import com.capstone.service.LearnerProgressService;
import com.capstone.service.LearnerRoadmapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/roadmap/")
@Tag(name = "Roadmap Controller", description = "Manage all roadmap's api")
public class RoadmapController {
    private final LearnerRoadmapService learnerRoadmapService;
    private final LearnerProgressService learnerProgressService;
    @PostMapping()
    @Operation(summary = "Assign track", description = "This end point allows a learner to select talent route")
    public ResponseEntity<String> assignRouteToLearner(@RequestBody RoadmapRequestDto roadmapRequestDto) {
        learnerRoadmapService.assignLearnerToTalentRoute(roadmapRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body("response");
    }

    @PostMapping("/start-progress")
    @Operation(summary = "Start progress", description = "This end point gets a learner to start tracking progress")
    public ResponseEntity<String> startTrackingProgress(@RequestBody StartAtomProgressRequestDto dto) {
        learnerProgressService.startAtomProgress(dto);
        return ResponseEntity.status(HttpStatus.OK).body("response");
    }

}
