package com.capstone.mapper;

import com.capstone.dto.response.SkillAtomSummaryDto;
import com.capstone.dto.response.SkillCapsuleResponseDto;
import com.capstone.model.CapsuleAtomMapping;
import com.capstone.model.SkillCapsuleSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SkillCapsuleMapper {

    @Mapping(target = "totalAtoms", expression = "java(entity.getCapsuleAtomMappings().size())")
    @Mapping(target = "atoms", ignore = true)
    SkillCapsuleResponseDto toBasicResponseDto(SkillCapsuleSnapshot entity);

    @Mapping(target = "totalAtoms", expression = "java(entity.getCapsuleAtomMappings().size())")
    @Mapping(target = "atoms", source = "capsuleAtomMappings")
    SkillCapsuleResponseDto toResponseDtoWithAtoms(SkillCapsuleSnapshot entity);

    @Mapping(source = "skillAtom.id", target = "id")
    @Mapping(source = "skillAtom.skillAtomId", target = "skillAtomId")
    @Mapping(source = "skillAtom.name", target = "atomName")
    @Mapping(source = "skillAtom.description", target = "description")
    @Mapping(target = "atomType", ignore = true)
    @Mapping(target = "difficultyLevel", ignore = true)
    @Mapping(target = "estimatedMinutes", ignore = true)
    SkillAtomSummaryDto toSkillAtomSummaryDto(CapsuleAtomMapping mapping);

    List<SkillAtomSummaryDto> toSkillAtomSummaryDtoList(List<CapsuleAtomMapping> mappings);
}