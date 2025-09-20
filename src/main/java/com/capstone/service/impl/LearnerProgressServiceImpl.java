package com.capstone.service.impl;

import com.capstone.dto.roadmap.StartAtomProgressRequestDto;
import com.capstone.exception.ProgressExistException;
import com.capstone.exception.ProgressNotFoundException;
import com.capstone.model.*;
import com.capstone.repository.LearnerAtomProgressRepository;
import com.capstone.service.LearnerProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LearnerProgressServiceImpl implements LearnerProgressService {
    private final LearnerAtomProgressRepository learnerAtomProgressRepository;

    @Transactional
    @Override
    public String startAtomProgress(StartAtomProgressRequestDto startAtomProgressRequestDto) {
        LearnerAtomProgress atomProgress = learnerAtomProgressRepository.findByLearnerIdAndAtomId(startAtomProgressRequestDto.getLearnerId(),
                startAtomProgressRequestDto.getAtomId(), startAtomProgressRequestDto.getTrackId())
                .orElseThrow(() -> new ProgressNotFoundException("Atom progress not found for this learner"));

        startAtomProgress(atomProgress); // start atom progress
        LearnerCapsuleProgress capsuleProgress = getCapsuleProgress(atomProgress); // start capsule progress
        startTrackProgress(capsuleProgress); // start growth track progress

        learnerAtomProgressRepository.save(atomProgress);

        return "Learner has started";
    }

    private void startAtomProgress(LearnerAtomProgress atomProgress){
        if(atomProgress.getStatus().equals(ProgressStatus.NOT_STARTED)){
            atomProgress.setStartedAt(LocalDateTime.now());
            atomProgress.setUpdatedAt(LocalDateTime.now());
            atomProgress.setStatus(ProgressStatus.IN_PROGRESS);
        }else{
            throw new ProgressExistException("You have already started your roadmap");
        }
    }

    private LearnerCapsuleProgress getCapsuleProgress(LearnerAtomProgress atomProgress){
        LearnerCapsuleProgress capsuleProgress = atomProgress.getLearnerCapsuleProgress();
        if(capsuleProgress.getStatus().equals(ProgressStatus.NOT_STARTED)){
            capsuleProgress.setStartedAt(LocalDateTime.now());
            capsuleProgress.setUpdatedAt(LocalDateTime.now());
            capsuleProgress.setStatus(ProgressStatus.IN_PROGRESS);
        }
        return capsuleProgress;
    }

    private void startTrackProgress(LearnerCapsuleProgress capsuleProgress){
        LearnerTrackProgress trackProgress = capsuleProgress.getLearnerTrackProgress();
        if(trackProgress.getStatus().equals(ProgressStatus.NOT_STARTED)){
            trackProgress.setStartedAt(LocalDateTime.now());
            trackProgress.setStartedAt(LocalDateTime.now());
            trackProgress.setStatus(ProgressStatus.IN_PROGRESS);
        }
    }
}
