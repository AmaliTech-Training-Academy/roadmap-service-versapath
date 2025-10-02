package com.capstone.mapper;

import com.capstone.dto.request.LearnerOnboardingRequestDto;
import com.capstone.dto.response.LearnerOnboardingResponseDto;
import com.capstone.model.LearnerOnboarding;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LearnerOnboardingMapper {

    @Mapping(source = "learner.userId", target = "learnerId")
    @Mapping(source = "learner.fullName", target = "learnerName")
    @Mapping(source = "talentRoute.talentRouteId", target = "talentRouteId")
    @Mapping(source = "talentRoute.routeName", target = "talentRouteName")
    @Mapping(source = "growthTrack.growthTrackId", target = "growthTrackId")
    @Mapping(source = "growthTrack.trackName", target = "growthTrackName")
    LearnerOnboardingResponseDto toBasicResponseDto(LearnerOnboarding entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "learner", ignore = true)
    @Mapping(target = "talentRoute", ignore = true)
    @Mapping(target = "growthTrack", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    LearnerOnboarding toEntity(LearnerOnboardingRequestDto requestDto);

    List<LearnerOnboardingResponseDto> toBasicResponseDtoList(List<LearnerOnboarding> entities);
}
