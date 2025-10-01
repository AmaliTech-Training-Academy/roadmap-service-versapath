package com.capstone.service;

import com.capstone.dto.request.AtomProgressRequestDto;
import com.capstone.dto.request.RecalculateProgressRequestDto;

public interface LearnerProgressService {
    void startAtomProgress(AtomProgressRequestDto atomProgressRequestDto);
    void completeAtomProgress(AtomProgressRequestDto atomProgressRequestDto);
    void recalculateAndUpdateLearnerRoadmap(RecalculateProgressRequestDto dto);
}
