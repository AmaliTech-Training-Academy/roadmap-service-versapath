package com.capstone.service.impl;

import com.capstone.dto.roadmap.StartAtomProgressRequestDto;
import com.capstone.exception.ProgressExistException;
import com.capstone.exception.ProgressNotFoundException;
import com.capstone.model.LearnerAtomProgress;
import com.capstone.model.ProgressStatus;
import com.capstone.repository.LearnerAtomProgressRepository;
import com.capstone.service.LearnerProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LearnerProgressServiceImpl implements LearnerProgressService {
    private final LearnerAtomProgressRepository learnerAtomProgressRepository;

    @Override
    public String startAtomProgress(StartAtomProgressRequestDto startAtomProgressRequestDto) {
        LearnerAtomProgress atomProgress = learnerAtomProgressRepository.findByLearnerIdAndAtomId(startAtomProgressRequestDto.getLearnerId(),
                startAtomProgressRequestDto.getAtomId(), startAtomProgressRequestDto.getTrackId())
                .orElseThrow(() -> new ProgressNotFoundException("Atom progress not found for this learner"));

        if(atomProgress.getStatus().equals(ProgressStatus.NOT_STARTED)){
            atomProgress.setStartedAt(LocalDateTime.now());
            atomProgress.setUpdatedAt(LocalDateTime.now());
            atomProgress.setStatus(ProgressStatus.IN_PROGRESS);
        }else{
            throw new ProgressExistException("You have already started your roadmap");
        }

        learnerAtomProgressRepository.save(atomProgress);

        return "Learner has started";
    }
}
