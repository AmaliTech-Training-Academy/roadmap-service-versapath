package com.capstone.service;

import com.capstone.dto.response.LearnerAtomProgressDto;
import com.capstone.dto.response.LearnerCapsuleProgressDto;
import com.capstone.dto.response.LearnerRoadmapWithProgressDto;
import com.capstone.dto.response.LearnerTrackProgressDto;
import com.capstone.dto.request.RoadmapRequestDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LearnerRoadmapService {
    void assignLearnerToTalentRoute(RoadmapRequestDto roadmapRequestDto);

    Optional<LearnerRoadmapWithProgressDto> getLearnerRoadmap(UUID learnerId);
    List<LearnerTrackProgressDto> getLearnerTracks(UUID learnerId);
    List<LearnerCapsuleProgressDto> getTrackCapsules(UUID learnerId, UUID trackId);
    List<LearnerAtomProgressDto> getCapsuleAtoms(UUID learnerId, UUID capsuleId);
}
