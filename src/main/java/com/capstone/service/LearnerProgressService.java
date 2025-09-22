package com.capstone.service;

import com.capstone.dto.roadmap.AtomProgressRequestDto;

public interface LearnerProgressService {
    String startAtomProgress(AtomProgressRequestDto atomProgressRequestDto);
    String completeAtomProgress(AtomProgressRequestDto atomProgressRequestDto);
}
