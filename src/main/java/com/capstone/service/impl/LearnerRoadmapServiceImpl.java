package com.capstone.service.impl;

import com.capstone.exception.TalentRouteNotFoundException;
import com.capstone.exception.UserNotFoundException;
import com.capstone.model.EnrollmentStatus;
import com.capstone.model.LearnerRoadmap;
import com.capstone.model.TalentRouteSnapshot;
import com.capstone.repository.LearnerRoadmapRepository;
import com.capstone.repository.TalentRouteSnapshotRepository;
import com.capstone.repository.UserSnapshotRepository;
import com.capstone.service.LearnerRoadmapService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LearnerRoadmapServiceImpl implements LearnerRoadmapService {
    private final LearnerRoadmapRepository learnerRoadmapRepository;
    private final TalentRouteSnapshotRepository talentRouteSnapshotRepository;
    private final UserSnapshotRepository userSnapshotRepository;
    @Override
    public String assignLearnerToTalentRoute(UUID learnerId, UUID talentRouteId) {

        LearnerRoadmap learnerRoadmap = createLearnerRoadmap(learnerId, talentRouteId);

        learnerRoadmapRepository.save(learnerRoadmap);
        return null;
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
}
