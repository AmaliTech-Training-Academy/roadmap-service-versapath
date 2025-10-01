package com.capstone.service;

import com.capstone.dto.request.LearnerOnboardingRequestDto;
import com.capstone.dto.response.LearnerOnboardingResponseDto;
import com.capstone.dto.response.PaginatedResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface LearnerOnboardingService {

    LearnerOnboardingResponseDto createOnboarding(LearnerOnboardingRequestDto requestDto);

    PaginatedResponseDto<LearnerOnboardingResponseDto> findAll(Pageable pageable);

    boolean existsByLearnerId(UUID learnerId);
}
