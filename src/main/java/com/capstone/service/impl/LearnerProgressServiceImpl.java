package com.capstone.service.impl;

import com.capstone.dto.roadmap.AtomProgressRequestDto;
import com.capstone.exception.ProgressExistException;
import com.capstone.exception.ProgressNotFoundException;
import com.capstone.model.*;
import com.capstone.repository.LearnerAtomProgressRepository;
import com.capstone.service.LearnerProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LearnerProgressServiceImpl implements LearnerProgressService {
    private final LearnerAtomProgressRepository learnerAtomProgressRepository;

    @Transactional
    @Override
    public String startAtomProgress(AtomProgressRequestDto atomProgressRequestDto) {
        LearnerAtomProgress atomProgress = learnerAtomProgressRepository.findByLearnerIdAndAtomId(atomProgressRequestDto.getLearnerId(),
                atomProgressRequestDto.getAtomId(), atomProgressRequestDto.getTrackId())
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

    @Transactional
    @Override
    public String completeAtomProgress(AtomProgressRequestDto atomProgressRequestDto) {
        LearnerAtomProgress atomProgress = learnerAtomProgressRepository.findByLearnerIdAndAtomId(atomProgressRequestDto.getLearnerId(),
                        atomProgressRequestDto.getAtomId(), atomProgressRequestDto.getTrackId())
                .orElseThrow(() -> new ProgressNotFoundException("Atom progress not found for this learner"));

        // update atom progress
        atomProgress.setStatus(ProgressStatus.COMPLETED);
        atomProgress.setCompletedAt(LocalDateTime.now());
        atomProgress.setCompleted(true);

        LearnerCapsuleProgress capsuleProgress = calculateCapsuleProgress(atomProgress);

        learnerAtomProgressRepository.save(atomProgress); //save atom progress

        return "Learner progress calculated";
    }

    private LearnerCapsuleProgress calculateCapsuleProgress(LearnerAtomProgress atomProgress){
        LearnerCapsuleProgress capsuleProgress = atomProgress.getLearnerCapsuleProgress();
        List<LearnerAtomProgress> atomProgressList = capsuleProgress.getLearnerAtomProgresses();

        // get number of completed atoms
        long completedAtomsNumber = atomProgressList.stream()
                .filter(LearnerAtomProgress::isCompleted)
                .count();

        // calculate percentage
        int capsulePercentage =  atomProgressList.isEmpty() ? 0 : (int)(completedAtomsNumber * 100)/atomProgressList.size();

        capsuleProgress.setProgressPercentage(capsulePercentage);

        return capsuleProgress;
    }


}
