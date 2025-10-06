package com.capstone.service;

import com.capstone.dto.request.AtomProgressRequestDto;
import com.capstone.dto.request.RecalculateProgressRequestDto;

import java.util.List;
import java.util.UUID;

public interface LearnerProgressService {
    void startAtomProgress(AtomProgressRequestDto atomProgressRequestDto);
    void completeAtomProgress(AtomProgressRequestDto atomProgressRequestDto);
    void recalculateAndUpdateLearnerRoadmap(RecalculateProgressRequestDto dto);
    void bulkRecalculateProgress(List<UUID> learnerIds, List<UUID> talentRouteIds);
}
