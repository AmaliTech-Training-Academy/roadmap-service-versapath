package com.capstone.mapper;

import com.capstone.dto.response.SkillAtomResponseDto;
import com.capstone.model.SkillAtomSnapshot;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SkillAtomMapper {

    SkillAtomResponseDto toResponseDto(SkillAtomSnapshot entity);
}