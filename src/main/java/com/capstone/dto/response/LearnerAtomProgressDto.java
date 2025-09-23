package com.capstone.dto.response;

import com.capstone.model.ProgressStatus;
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
public class LearnerAtomProgressDto {
    private UUID atomProgressId;
    private UUID atomId;
    private String name;
    private String description;
    private ProgressStatus status;
    private boolean isCompleted;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Boolean isUnlocked;
}