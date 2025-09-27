package com.capstone.mapper;

import com.capstone.dto.response.MentorResponseDto;
import com.capstone.dto.response.MentorSpecializationDto;
import com.capstone.model.MentorSnapshot;
import com.capstone.model.MentorRouteMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MentorMapper {

    @Mapping(source = "mentorId", target = "mentorId")
    @Mapping(source = "id", target = "id")
    @Mapping(target = "specializations", ignore = true)
    MentorResponseDto toBasicResponseDto(MentorSnapshot mentorSnapshot);

    @Mapping(source = "mentorId", target = "mentorId")
    @Mapping(source = "id", target = "id")
    @Mapping(source = "mentorRouteMappings", target = "specializations")
    MentorResponseDto toResponseDtoWithSpecializations(MentorSnapshot mentorSnapshot);

    @Mapping(source = "id", target = "mappingId")
    @Mapping(source = "talentRoute.talentRouteId", target = "talentRouteId")
    @Mapping(source = "talentRoute.routeName", target = "routeName")
    @Mapping(source = "talentRoute.description", target = "routeDescription")
    @Mapping(source = "createdAt", target = "assignedAt")
    MentorSpecializationDto toSpecializationDto(MentorRouteMapping mentorRouteMapping);

    List<MentorSpecializationDto> toSpecializationDtoList(List<MentorRouteMapping> mentorRouteMappings);

}
