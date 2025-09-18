package com.capstone.dto.response;

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
public class SkillAtomResponseDto {
    private UUID id;
    private UUID skillAtomId;
    private String name;
    private String description;
    private Integer moodleModuleId;
    private Integer moodlePageId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}