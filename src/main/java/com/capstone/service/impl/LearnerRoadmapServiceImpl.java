package com.capstone.service.impl;

import com.capstone.repository.LearnerRoadmapRepository;
import com.capstone.service.LearnerRoadmapService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LearnerRoadmapServiceImpl implements LearnerRoadmapService {
    private final LearnerRoadmapRepository learnerRoadmapRepository;
    @Override
    public String assignLearnerToTalentRoute(UUID learnerId, UUID talentRouteId) {
        //TODO: assign learner to talent route
        return null;
    }
}
