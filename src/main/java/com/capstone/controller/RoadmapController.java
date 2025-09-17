package com.capstone.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("roadmap")
@Tag(name = "Roadmap Controller", description = "Manage all roadmap's api")
public class RoadmapController {
    @GetMapping()
    @Operation(summary = "Assign track", description = "This end point allows a learner to select talent route")
    public ResponseEntity<String> assignRouteToLearner() {
        //TODO: assign talent route to a learner
        return ResponseEntity.status(HttpStatus.OK).body("response");
    }

}
