package com.capstone.service;

import com.capstone.dto.roadmap.AtomProgressRequestDto;
import com.capstone.dto.roadmap.RecalculateProgressRequestDto;

public interface LearnerProgressService {
    void startAtomProgress(AtomProgressRequestDto atomProgressRequestDto);
    void completeAtomProgress(AtomProgressRequestDto atomProgressRequestDto);
    void recalculateAndUpdateLearnerRoadmap(RecalculateProgressRequestDto dto);
}
