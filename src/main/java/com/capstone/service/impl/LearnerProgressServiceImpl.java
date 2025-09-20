package com.capstone.service.impl;

import com.capstone.dto.roadmap.AtomProgressRequestDto;
import com.capstone.dto.roadmap.RecalculateProgressRequestDto;
import com.capstone.exception.ProgressExistException;
import com.capstone.exception.ProgressNotFoundException;
import com.capstone.exception.TalentRouteNotFoundException;
import com.capstone.model.*;
import com.capstone.repository.GrowthTrackCapsuleMappingRepository;
import com.capstone.repository.LearnerAtomProgressRepository;
import com.capstone.repository.LearnerRoadmapRepository;
import com.capstone.repository.RouteTrackMappingRepository;
import com.capstone.service.LearnerProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearnerProgressServiceImpl implements LearnerProgressService {
    private final LearnerAtomProgressRepository learnerAtomProgressRepository;
    private final LearnerRoadmapRepository learnerRoadmapRepository;
    private final RouteTrackMappingRepository routeTrackMappingRepository;
    private final GrowthTrackCapsuleMappingRepository growthTrackCapsuleMappingRepository;

    @Transactional
    @Override
    public String startAtomProgress(AtomProgressRequestDto atomProgressRequestDto) {
        LearnerAtomProgress atomProgress = learnerAtomProgressRepository.findByLearnerIdAndAtomId(atomProgressRequestDto.getLearnerId(),
                atomProgressRequestDto.getAtomId(), atomProgressRequestDto.getTrackId())
                .orElseThrow(() -> new ProgressNotFoundException("Atom progress not found for this learner"));

        startAtomProgress(atomProgress); // start atom progress
        LearnerCapsuleProgress capsuleProgress = getCapsuleProgress(atomProgress); // start capsule progress
        startGrowthTrackProgress(capsuleProgress); // start growth track progress

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

    private void startGrowthTrackProgress(LearnerCapsuleProgress capsuleProgress){
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

        learnerAtomProgressRepository.save(atomProgress); //save atom progress

        LearnerCapsuleProgress capsuleProgress = calculateCapsuleProgress(atomProgress);
        LearnerTrackProgress growthTrackProgress = calculateGrowthTrackProgress(capsuleProgress);
        LearnerRoadmap learnerRoadmap = calculateRoadmap(growthTrackProgress);

        learnerRoadmapRepository.save(learnerRoadmap); // save roadmap progress

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

    private LearnerTrackProgress calculateGrowthTrackProgress(LearnerCapsuleProgress capsuleProgress){
        LearnerTrackProgress growthTrackProgress = capsuleProgress.getLearnerTrackProgress();
        List<LearnerCapsuleProgress> capsuleProgressList = growthTrackProgress.getLearnerCapsuleProgresses();

        // get the sum of capsule percentages
        long sumOfCapsulesPercentage = capsuleProgressList.stream()
                .map(LearnerCapsuleProgress::getProgressPercentage)
                .reduce(0, Integer::sum);

        // calculate percentage
        int trackPercentage = capsuleProgressList.isEmpty() ? 0 : (int)(sumOfCapsulesPercentage/capsuleProgressList.size());

        growthTrackProgress.setProgressPercentage(trackPercentage);

        return growthTrackProgress;
    }

    private LearnerRoadmap calculateRoadmap(LearnerTrackProgress growthTrackProgress){
        LearnerRoadmap learnerRoadmap = growthTrackProgress.getLearnerRoadmap();
        List<LearnerTrackProgress> growthTrackProgressList = learnerRoadmap.getLearnerTrackProgresses();

        // get the sum of growth track percentages
        long sumOfGrowthTracksPercentage = growthTrackProgressList.stream()
                .map(LearnerTrackProgress::getProgressPercentage)
                .reduce(0, Integer::sum);

        // calculate percentage
        int learnerRoadmapPercentage = growthTrackProgressList.isEmpty() ? 0 : (int)(sumOfGrowthTracksPercentage/growthTrackProgressList.size());

        learnerRoadmap.setOverallProgressPercentage(learnerRoadmapPercentage);

        return learnerRoadmap;
    }

    @Transactional
    @Override
    public String recalculateAndUpdateLearnerRoadmap(RecalculateProgressRequestDto dto) {
        LearnerRoadmap talentRoute = learnerRoadmapRepository
                .findByUserIdAndTalentRouteId(dto.getLearnerId(), dto.getTalentRouteId())
                .orElseThrow(() -> new TalentRouteNotFoundException("The learner isn't enrolled in this roadmap"));

        updateLearnerRoadmap(talentRoute); // first update learner roadmap
        return null;
    }

    private void updateLearnerRoadmap(LearnerRoadmap talentRoute){
        // find all growth tracks that belong to talent route (new ones + existing in the roadmap)
        List<GrowthTrackSnapshot> allGrowthTracks = routeTrackMappingRepository
                .findGrowthTracksByTalentRouteId(talentRoute.getTalentRoute().getTalentRouteId());

        // find the existing growth tracks in the roadmap
        Map<UUID, LearnerTrackProgress> existingTracks = talentRoute.getLearnerTrackProgresses().stream()
                .collect(Collectors.toMap(tp -> tp.getGrowthTrack().getGrowthTrackId(), tp -> tp));

        for(GrowthTrackSnapshot growthTrack: allGrowthTracks){
            UUID growthTrackId = growthTrack.getGrowthTrackId();
            LearnerTrackProgress learnerGrowthTrackProgress = existingTracks.get(growthTrackId);
            if (learnerGrowthTrackProgress == null) { // add only the ones that aren't in the roadmap
                // add new growth tracks to this learner roadmap
                learnerGrowthTrackProgress = LearnerTrackProgress.builder()
                        .learnerRoadmap(talentRoute)
                        .growthTrack(growthTrack)
                        .status(ProgressStatus.NOT_STARTED)
                        .progressPercentage(0)
                        .build();

                talentRoute.getLearnerTrackProgresses().add(learnerGrowthTrackProgress); // add growth track to roadmap

                existingTracks.put(growthTrackId, learnerGrowthTrackProgress); // update learnerTrackProgress
            }

        }
    }
}
