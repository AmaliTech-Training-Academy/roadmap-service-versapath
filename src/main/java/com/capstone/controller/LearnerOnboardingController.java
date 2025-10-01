package com.capstone.controller;

import com.capstone.dto.request.LearnerOnboardingRequestDto;
import com.capstone.dto.response.ApiResponseDto;
import com.capstone.dto.response.LearnerOnboardingResponseDto;
import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.service.LearnerOnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/roadmap/learner-onboarding")
@RequiredArgsConstructor
@Slf4j
public class LearnerOnboardingController {

    private final LearnerOnboardingService learnerOnboardingService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<LearnerOnboardingResponseDto>> createOnboarding(
            @Valid @RequestBody LearnerOnboardingRequestDto requestDto) {

        log.info("Creating learner onboarding for learner: {}", requestDto.getLearnerId());

        LearnerOnboardingResponseDto responseDto = learnerOnboardingService.createOnboarding(requestDto);

        return ResponseEntity.ok(ApiResponseDto.success(responseDto, "Learner onboarded successfully"));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<LearnerOnboardingResponseDto>>> getAllOnboardings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        log.info("Fetching learner onboardings: page={}, size={}, sort={}, direction={}",
                page, size, sort, direction);

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PaginatedResponseDto<LearnerOnboardingResponseDto> result =
                learnerOnboardingService.findAll(pageable);

        return ResponseEntity.ok(ApiResponseDto.success(result, "Learner onboardings retrieved successfully"));
    }
}
