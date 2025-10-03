package com.capstone.dto.response;

import com.capstone.model.ProgressStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LearnerTrackProgressDto {
    private UUID trackProgressId;
    private UUID trackId;
    private String trackName;
    private String description;
    private Integer sequenceOrder;
    private ProgressStatus status;
    private Integer progressPercentage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Boolean isUnlocked;
    private List<LearnerCapsuleProgressDto> capsules;
}