package com.capstone.mapper;

import com.capstone.model.GrowthTrackSnapshot;
import org.common.event.GrowthTrackEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface GrowthTrackEventMapper {

    @Mapping(source = "id", target = "growthTrackId")
    @Mapping(source = "name", target = "trackName")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "learnerTrackProgresses", ignore = true)
    @Mapping(target = "trackCapsuleMappings", ignore = true)
    GrowthTrackSnapshot toGrowthTrackSnapshot(GrowthTrackEvent event);

    @Mapping(source = "name", target = "trackName")
    @Mapping(target = "growthTrackId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "learnerTrackProgresses", ignore = true)
    @Mapping(target = "trackCapsuleMappings", ignore = true)
    void updateGrowthTrackSnapshot(GrowthTrackEvent event, @MappingTarget GrowthTrackSnapshot growthTrackSnapshot);
}