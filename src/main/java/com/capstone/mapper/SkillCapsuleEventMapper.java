package com.capstone.mapper;

import com.capstone.model.SkillCapsuleSnapshot;
import org.common.event.SkillCapsuleEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SkillCapsuleEventMapper {


    @Mapping(source = "id", target = "skillCapsuleId")
    @Mapping(source = "name", target = "capsuleName")
    @Mapping(source = "difficulty", target = "difficultyLevel")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "learnerCapsuleProgresses", ignore = true)
    @Mapping(target = "capsuleAtomMappings", ignore = true)
    SkillCapsuleSnapshot toSkillCapsuleSnapshot(SkillCapsuleEvent event);


    @Mapping(source = "name", target = "capsuleName")
    @Mapping(source = "difficulty", target = "difficultyLevel")
    @Mapping(target = "skillCapsuleId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "learnerCapsuleProgresses", ignore = true)
    @Mapping(target = "capsuleAtomMappings", ignore = true)
    void updateSkillCapsuleSnapshot(SkillCapsuleEvent event, @MappingTarget SkillCapsuleSnapshot skillCapsuleSnapshot);
}

