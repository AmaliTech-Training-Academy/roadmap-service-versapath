package com.capstone.dto.response;

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
public class SkillCapsuleResponseDto {
    private UUID id;
    private UUID skillCapsuleId;
    private String capsuleName;
    private String description;
    private String difficultyLevel;
    private String proficiencyLevel;
    private Integer moodleCourseId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer totalAtoms;
    private List<SkillAtomSummaryDto> atoms;
}