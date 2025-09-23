package com.capstone.dto.response;

import com.capstone.model.EnrollmentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LearnerRoadmapWithProgressDto {
    private UUID learnerId;
    private UUID roadmapId;
    private UUID talentRouteId;
    private String routeName;
    private String routeDescription;
    private EnrollmentStatus enrollmentStatus;
    private Integer overallProgressPercentage;
    private LocalDateTime enrolledAt;
}