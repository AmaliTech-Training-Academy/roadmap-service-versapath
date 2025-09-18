package com.capstone.service.impl;

import com.capstone.dto.route.RoadmapRequestDto;
import com.capstone.exception.TalentRouteNotFoundException;
import com.capstone.exception.UserNotFoundException;
import com.capstone.model.*;
import com.capstone.repository.LearnerRoadmapRepository;
import com.capstone.repository.RouteTrackMappingRepository;
import com.capstone.repository.TalentRouteSnapshotRepository;
import com.capstone.repository.UserSnapshotRepository;
import com.capstone.service.LearnerRoadmapService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LearnerRoadmapServiceImpl implements LearnerRoadmapService {
    private final LearnerRoadmapRepository learnerRoadmapRepository;
    private final TalentRouteSnapshotRepository talentRouteSnapshotRepository;
    private final UserSnapshotRepository userSnapshotRepository;
    private final RouteTrackMappingRepository routeTrackMappingRepository;
    @Override
    public String assignLearnerToTalentRoute(RoadmapRequestDto roadmapRequestDto) {

        LearnerRoadmap learnerRoadmap = createLearnerRoadmap(roadmapRequestDto.getLearnerId(),
                roadmapRequestDto.getTalentRouteId());

        // fetch all the growth tracks belong to the talent route
        List<GrowthTrackSnapshot> growthTracks = routeTrackMappingRepository
                .findGrowthTracksByTalentRouteId(roadmapRequestDto.getTalentRouteId());

        List<LearnerTrackProgress> trackProgresses = createTrackProgresses(growthTracks, learnerRoadmap);

        learnerRoadmap.setLearnerTrackProgresses(trackProgresses); // map roadmap to growth track progress

        learnerRoadmapRepository.save(learnerRoadmap);
        return "Talent route assigned!";
    }

    private LearnerRoadmap createLearnerRoadmap(UUID learnerId, UUID talentRouteId){
        TalentRouteSnapshot talentRoute = talentRouteSnapshotRepository.findByTalentRouteId(talentRouteId)
                .orElseThrow( () -> new TalentRouteNotFoundException("A talent route provided doesn't exist")
                );

        if(userSnapshotRepository.findByUserId(learnerId).isEmpty()){
            throw new UserNotFoundException("A leaner provided doesn't exist");
        }

        return LearnerRoadmap.builder()
                .talentRoute(talentRoute)
                .userId(learnerId)
                .enrollmentStatus(EnrollmentStatus.ACTIVE)
                .overallProgressPercentage(0)
                .build();
    }

    private List<LearnerTrackProgress> createTrackProgresses(List<GrowthTrackSnapshot> growthTracks, LearnerRoadmap learnerRoadmap){
        return growthTracks.stream()
                .map(track -> LearnerTrackProgress.builder()
                            .learnerRoadmap(learnerRoadmap)
                            .growthTrack(track)
                            .status(ProgressStatus.NOT_STARTED)
                            .progressPercentage(0)
                            .build()
                )
                .toList();
    }
}
