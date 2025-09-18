package com.capstone.mapper;

import com.capstone.dto.response.GrowthTrackResponseDto;
import com.capstone.dto.response.SkillCapsuleSummaryDto;
import com.capstone.model.GrowthTrackSnapshot;
import com.capstone.model.TrackCapsuleMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GrowthTrackMapper {

    @Mapping(target = "totalCapsules", expression = "java(entity.getTrackCapsuleMappings().size())")
    @Mapping(target = "capsules", ignore = true)
    GrowthTrackResponseDto toBasicResponseDto(GrowthTrackSnapshot entity);

    @Mapping(target = "totalCapsules", expression = "java(entity.getTrackCapsuleMappings().size())")
    @Mapping(target = "capsules", source = "trackCapsuleMappings")
    GrowthTrackResponseDto toResponseDtoWithCapsules(GrowthTrackSnapshot entity);

    @Mapping(target = "totalAtoms", expression = "java(mapping.getSkillCapsule().getCapsuleAtomMappings().size())")
    @Mapping(source = "skillCapsule.id", target = "id")
    @Mapping(source = "skillCapsule.skillCapsuleId", target = "skillCapsuleId")
    @Mapping(source = "skillCapsule.capsuleName", target = "capsuleName")
    @Mapping(source = "skillCapsule.description", target = "description")
    @Mapping(source = "skillCapsule.difficultyLevel", target = "difficultyLevel")
    @Mapping(source = "skillCapsule.proficiencyLevel", target = "proficiencyLevel")
    SkillCapsuleSummaryDto toSkillCapsuleSummaryDto(TrackCapsuleMapping mapping);

    List<SkillCapsuleSummaryDto> toSkillCapsuleSummaryDtoList(List<TrackCapsuleMapping> mappings);
}