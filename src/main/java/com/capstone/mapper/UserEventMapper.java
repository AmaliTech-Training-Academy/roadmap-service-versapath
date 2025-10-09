package com.capstone.mapper;

import com.capstone.model.UserSnapshot;
import org.common.event.ProduceUserEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserEventMapper {

    @Mapping(source = "versapathUserId", target = "userId")
    @Mapping(source = "imageUrl", target = "image")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserSnapshot toUserSnapshot(ProduceUserEvent event);

    @Mapping(target = "userId", ignore = true)
    @Mapping(source = "imageUrl", target = "image")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateUserSnapshot(ProduceUserEvent event, @MappingTarget UserSnapshot userSnapshot);
}
