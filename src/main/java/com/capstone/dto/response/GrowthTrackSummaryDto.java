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
public class GrowthTrackSummaryDto {
    private UUID id;
    private UUID growthTrackId;
    private String trackName;
    private String description;
    private Integer sequenceOrder;
    private Integer totalCapsules;
}