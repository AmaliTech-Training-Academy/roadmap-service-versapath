package com.capstone.mapper;

import com.capstone.model.MentorSnapshot;
import org.common.event.ProduceMentorEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MentorEventMapper {

    @Mapping(source = "versapathUserId", target = "mentorId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignedLearner", constant = "0")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "mentorRouteMappings", ignore = true)
    MentorSnapshot toMentorSnapshot(ProduceMentorEvent event);

    @Mapping(target = "mentorId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "mentorRouteMappings", ignore = true)
    void updateMentorSnapshot(ProduceMentorEvent event, @MappingTarget MentorSnapshot mentorSnapshot);
}
