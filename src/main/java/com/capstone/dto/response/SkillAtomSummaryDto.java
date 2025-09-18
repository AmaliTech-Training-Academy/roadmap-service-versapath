package com.capstone.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SkillAtomSummaryDto {
    private UUID id;
    private UUID skillAtomId;
    private String atomName;
    private String description;
    private String atomType;
    private String difficultyLevel;
    private Integer sequenceOrder;
    private Integer estimatedMinutes;
}