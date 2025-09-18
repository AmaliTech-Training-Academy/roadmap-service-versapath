package com.capstone.service;

import java.util.UUID;

public interface LearnerRoadmapService {
    String assignLearnerToTalentRoute(UUID learnerId, UUID talentRouteId);
}
