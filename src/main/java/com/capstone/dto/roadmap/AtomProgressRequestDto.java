package com.capstone.dto.roadmap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtomProgressRequestDto {
    UUID learnerId;
    UUID atomId;
    UUID capsuleId;
    UUID trackId;
    UUID talentRouteId;
}
