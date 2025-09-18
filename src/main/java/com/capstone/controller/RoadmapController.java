package com.capstone.controller;

import com.capstone.dto.route.RoadmapRequestDto;
import com.capstone.service.LearnerRoadmapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("roadmap")
@Tag(name = "Roadmap Controller", description = "Manage all roadmap's api")
public class RoadmapController {
    private final LearnerRoadmapService learnerRoadmapService;
    @PostMapping()
    @Operation(summary = "Assign track", description = "This end point allows a learner to select talent route")
    public ResponseEntity<String> assignRouteToLearner(@RequestBody RoadmapRequestDto roadmapRequestDto) {
        learnerRoadmapService.assignLearnerToTalentRoute(roadmapRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body("response");
    }

}
