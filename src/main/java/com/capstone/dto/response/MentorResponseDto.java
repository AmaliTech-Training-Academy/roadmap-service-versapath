package com.capstone.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MentorResponseDto {
    private UUID id;
    private UUID mentorId;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private Integer totalAssignedLearners;
    private List<MentorSpecializationDto> specializations;
}
