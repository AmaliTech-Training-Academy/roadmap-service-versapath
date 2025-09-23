package com.capstone.mapper;

import com.capstone.dto.response.LearnerAtomProgressDto;
import com.capstone.dto.response.LearnerCapsuleProgressDto;
import com.capstone.dto.response.LearnerRoadmapWithProgressDto;
import com.capstone.dto.response.LearnerTrackProgressDto;
import com.capstone.model.LearnerAtomProgress;
import com.capstone.model.LearnerCapsuleProgress;
import com.capstone.model.LearnerRoadmap;
import com.capstone.model.LearnerTrackProgress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LearnerRoadmapViewMapper {

    // Main roadmap mapping
    @Mapping(source = "userId", target = "learnerId")
    @Mapping(source = "id", target = "roadmapId")
    @Mapping(source = "talentRoute.talentRouteId", target = "talentRouteId")
    @Mapping(source = "talentRoute.routeName", target = "routeName")
    @Mapping(source = "talentRoute.description", target = "routeDescription")
    @Mapping(source = "createdAt", target = "enrolledAt")
    LearnerRoadmapWithProgressDto toLearnerRoadmapDto(LearnerRoadmap learnerRoadmap);

    // Track progress mapping
    @Mapping(source = "id", target = "trackProgressId")
    @Mapping(source = "growthTrack.growthTrackId", target = "trackId")
    @Mapping(source = "growthTrack.trackName", target = "trackName")
    @Mapping(source = "growthTrack.description", target = "description")
    @Mapping(target = "sequenceOrder", ignore = true)
    @Mapping(target = "isUnlocked", ignore = true)
    LearnerTrackProgressDto toLearnerTrackProgressDto(LearnerTrackProgress trackProgress);

    List<LearnerTrackProgressDto> toLearnerTrackProgressDtoList(List<LearnerTrackProgress> trackProgresses);

    // Capsule progress mapping
    @Mapping(source = "id", target = "capsuleProgressId")
    @Mapping(source = "skillCapsule.skillCapsuleId", target = "capsuleId")
    @Mapping(source = "skillCapsule.capsuleName", target = "capsuleName")
    @Mapping(source = "skillCapsule.description", target = "description")
    @Mapping(target = "sequenceOrder", ignore = true)
    @Mapping(target = "isUnlocked", ignore = true)
    LearnerCapsuleProgressDto toLearnerCapsuleProgressDto(LearnerCapsuleProgress capsuleProgress);

    List<LearnerCapsuleProgressDto> toLearnerCapsuleProgressDtoList(List<LearnerCapsuleProgress> capsuleProgresses);

    // Atom progress mapping
    @Mapping(source = "id", target = "atomProgressId")
    @Mapping(source = "skillAtom.skillAtomId", target = "atomId")
    @Mapping(source = "skillAtom.name", target = "name")
    @Mapping(source = "skillAtom.description", target = "description")
    @Mapping(source = "completed", target = "isCompleted")
    @Mapping(target = "isUnlocked", ignore = true)
    LearnerAtomProgressDto toLearnerAtomProgressDto(LearnerAtomProgress atomProgress);

    List<LearnerAtomProgressDto> toLearnerAtomProgressDtoList(List<LearnerAtomProgress> atomProgresses);
}