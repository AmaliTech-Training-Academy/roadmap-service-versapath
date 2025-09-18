package com.capstone.mapper;

import com.capstone.model.TalentRouteSnapshot;
import org.common.event.TalentRouteEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TalentRouteEventMapper {

    @Mapping(source = "id", target = "talentRouteId")
    @Mapping(source = "name", target = "routeName")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "learnerRoadmaps", ignore = true)
    @Mapping(target = "routeTrackMappings", ignore = true)
    TalentRouteSnapshot toTalentRouteSnapshot(TalentRouteEvent event);

    @Mapping(source = "name", target = "routeName")
    @Mapping(target = "talentRouteId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "learnerRoadmaps", ignore = true)
    @Mapping(target = "routeTrackMappings", ignore = true)
    void updateTalentRouteSnapshot(TalentRouteEvent event, @MappingTarget TalentRouteSnapshot talentRouteSnapshot);
}