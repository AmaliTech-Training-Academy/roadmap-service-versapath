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
    private String learnerName;
    private UUID talentRouteId;
    private String talentRouteName;
    private UUID growthTrackId;
    private String growthTrackName;
    private LocalDateTime createdAt;
}
