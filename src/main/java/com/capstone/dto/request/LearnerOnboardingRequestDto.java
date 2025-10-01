package com.capstone.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearnerOnboardingRequestDto {

    @NotNull(message = "Learner ID is required")
    private UUID learnerId;

    @NotNull(message = "Talent route ID is required")
    private UUID talentRouteId;

    @NotNull(message = "Growth track ID is required")
    private UUID growthTrackId;
}
