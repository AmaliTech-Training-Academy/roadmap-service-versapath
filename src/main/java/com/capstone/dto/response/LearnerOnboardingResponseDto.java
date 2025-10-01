package com.capstone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearnerOnboardingResponseDto {

    private UUID id;
    private UUID learnerId;
    private UUID talentRouteId;
    private UUID growthTrackId;
    private LocalDateTime createdAt;
}
