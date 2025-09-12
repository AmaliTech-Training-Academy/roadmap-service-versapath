package com.capstone.mapper;

import com.capstone.model.SkillAtomSnapshot;
import org.common.event.SkillAtomEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SkillAtomEventMapper {

    @Mapping(source = "id", target = "skillAtomId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "learnerAtomProgresses", ignore = true)
    SkillAtomSnapshot toSkillAtomSnapshot(SkillAtomEvent event);

    @Mapping(target = "skillAtomId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "learnerAtomProgresses", ignore = true)
    void updateSkillAtomSnapshot(SkillAtomEvent event, @MappingTarget SkillAtomSnapshot skillAtomSnapshot);
}
