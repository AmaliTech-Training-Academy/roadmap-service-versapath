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
public class GrowthTrackResponseDto {
    private UUID id;
    private UUID growthTrackId;
    private String trackName;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer totalCapsules;
    private List<SkillCapsuleSummaryDto> capsules;
}