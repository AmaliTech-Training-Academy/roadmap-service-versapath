package com.capstone.mapper;

import com.capstone.dto.response.LearnerDto;
import com.capstone.model.UserSnapshot;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    LearnerDto toDto(UserSnapshot entity);

}
