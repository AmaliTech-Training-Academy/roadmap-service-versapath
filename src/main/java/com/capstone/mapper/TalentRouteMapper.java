package com.capstone.mapper;

import com.capstone.dto.response.GrowthTrackSummaryDto;
import com.capstone.dto.response.TalentRouteResponseDto;
import com.capstone.model.RouteTrackMapping;
import com.capstone.model.TalentRouteSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TalentRouteMapper {

    @Mapping(target = "totalTracks", expression = "java(entity.getRouteTrackMappings().size())")
    @Mapping(target = "tracks", ignore = true)
    TalentRouteResponseDto toBasicResponseDto(TalentRouteSnapshot entity);

    @Mapping(target = "totalTracks", expression = "java(entity.getRouteTrackMappings().size())")
    @Mapping(target = "tracks", source = "routeTrackMappings")
    TalentRouteResponseDto toResponseDtoWithTracks(TalentRouteSnapshot entity);

    @Mapping(target = "totalCapsules", expression = "java(mapping.getGrowthTrack().getTrackCapsuleMappings().size())")
    @Mapping(source = "growthTrack.id", target = "id")
    @Mapping(source = "growthTrack.growthTrackId", target = "growthTrackId")
    @Mapping(source = "growthTrack.trackName", target = "trackName")
    @Mapping(source = "growthTrack.description", target = "description")
    @Mapping(source = "growthTrack.image", target = "image")
    GrowthTrackSummaryDto toGrowthTrackSummaryDto(RouteTrackMapping mapping);

    List<GrowthTrackSummaryDto> toGrowthTrackSummaryDtoList(List<RouteTrackMapping> mappings);
}