package com.capstone.service;

import com.capstone.dto.roadmap.AtomProgressRequestDto;
import com.capstone.dto.roadmap.RecalculateProgressRequestDto;

import java.util.UUID;

public interface LearnerProgressService {
    String startAtomProgress(AtomProgressRequestDto atomProgressRequestDto);
    String completeAtomProgress(AtomProgressRequestDto atomProgressRequestDto);
    String recalculateAndUpdateLearnerRoadmap(RecalculateProgressRequestDto dto);
}
