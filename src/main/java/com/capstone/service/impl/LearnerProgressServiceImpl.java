package com.capstone.service.impl;

import com.capstone.dto.request.AtomProgressRequestDto;
import com.capstone.dto.request.RecalculateProgressRequestDto;
import com.capstone.exception.*;
import com.capstone.model.*;
import com.capstone.repository.*;
import com.capstone.service.LearnerProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final CapsuleAtomMappingRepository capsuleAtomMappingRepository;
    private final LearnerTrackProgressRepository learnerTrackProgressRepository;

    @Transactional
    @Override
    public void startAtomProgress(AtomProgressRequestDto atomProgressRequestDto) {
        LearnerAtomProgress atomProgress = learnerAtomProgressRepository.findByLearnerIdAndAtomId(atomProgressRequestDto.getLearnerId(),
                atomProgressRequestDto.getAtomId(), atomProgressRequestDto.getTrackId(),
                        atomProgressRequestDto.getCapsuleId(), atomProgressRequestDto.getTalentRouteId())
                .orElseThrow(() -> new ProgressNotFoundException("Atom progress not found for this learner"));

        startAtomProgress(atomProgress); // start atom progress
        LearnerCapsuleProgress capsuleProgress = getCapsuleProgress(atomProgress); // start capsule progress
        startGrowthTrackProgress(atomProgressRequestDto.getLearnerId(), capsuleProgress); // start growth track progress

        learnerAtomProgressRepository.save(atomProgress);

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

    private void startGrowthTrackProgress(UUID learnerId, LearnerCapsuleProgress capsuleProgress){
        LearnerTrackProgress trackProgress = capsuleProgress.getLearnerTrackProgress();
        List<LearnerTrackProgress> learnerTrackProgressList = learnerTrackProgressRepository
                .findTrackProgressByUserIdAndTrackId(learnerId, trackProgress.getGrowthTrack().getGrowthTrackId());
        //find an ongoing learner growth track
        if(!learnerTrackProgressList.isEmpty()){
            long uncompletedGrowthTrackNumber = learnerTrackProgressList.stream()
                    .filter(track->track.getStatus().equals(ProgressStatus.IN_PROGRESS))
                    .count();
            if(uncompletedGrowthTrackNumber >=1 ){
                throw new RoadmapExistException("You have an ongoing growth track, you cannot start a new one!");
            }
        }

        if(trackProgress.getStatus().equals(ProgressStatus.NOT_STARTED)){
            trackProgress.setStartedAt(LocalDateTime.now());
            trackProgress.setStartedAt(LocalDateTime.now());
            trackProgress.setStatus(ProgressStatus.IN_PROGRESS);
        }
    }

    @Transactional
    @Override
    public void completeAtomProgress(AtomProgressRequestDto atomProgressRequestDto) {
        LearnerAtomProgress atomProgress = learnerAtomProgressRepository.findByLearnerIdAndAtomId(atomProgressRequestDto.getLearnerId(),
                        atomProgressRequestDto.getAtomId(), atomProgressRequestDto.getTrackId(),
                        atomProgressRequestDto.getCapsuleId(), atomProgressRequestDto.getTalentRouteId())
                .orElseThrow(() -> new ProgressNotFoundException("Atom progress not found for this learner"));

        if(atomProgress.getStatus().equals(ProgressStatus.IN_PROGRESS)){
            // update atom progress
            atomProgress.setStatus(ProgressStatus.COMPLETED);
            atomProgress.setCompletedAt(LocalDateTime.now());
            atomProgress.setCompleted(true);
        }else if(atomProgress.getStatus().equals(ProgressStatus.NOT_STARTED)){
            throw new LessonException("You haven't started learning this lesson");
        }
        else{
            throw new LessonException("You have already completed this lesson");
        }

        learnerAtomProgressRepository.save(atomProgress); //save atom progress

        LearnerCapsuleProgress capsuleProgress = calculateCapsuleProgress(atomProgress);
        LearnerTrackProgress growthTrackProgress = calculateGrowthTrackProgress(capsuleProgress);
        LearnerRoadmap learnerRoadmap = calculateRoadmap(growthTrackProgress);

        learnerRoadmapRepository.save(learnerRoadmap); // save roadmap progress

    }

    private LearnerCapsuleProgress calculateCapsuleProgress(LearnerAtomProgress atomProgress){
        LearnerCapsuleProgress capsuleProgress = atomProgress.getLearnerCapsuleProgress();
        List<LearnerAtomProgress> atomProgressList = capsuleProgress.getLearnerAtomProgresses();

        // get number of completed atoms
        long completedAtomsNumber = atomProgressList.stream()
                .filter(LearnerAtomProgress::isCompleted)
                .count();

        // calculate percentage
        double capsulePercentage =  atomProgressList.isEmpty() ? 0.0 : (completedAtomsNumber * 100.0)/atomProgressList.size();
        double roundedPercentage = Math.ceil(capsulePercentage * 10.0) / 10.0;

        if(roundedPercentage >= 100.0){
            capsuleProgress.setStatus(ProgressStatus.COMPLETED);
            capsuleProgress.setCompletedAt(LocalDateTime.now());
        }
        capsuleProgress.setProgressPercentage(roundedPercentage);

        return capsuleProgress;
    }

    private LearnerTrackProgress calculateGrowthTrackProgress(LearnerCapsuleProgress capsuleProgress){
        LearnerTrackProgress growthTrackProgress = capsuleProgress.getLearnerTrackProgress();
        List<LearnerCapsuleProgress> capsuleProgressList = growthTrackProgress.getLearnerCapsuleProgresses();

        // get the sum of capsule percentages
        double sumOfCapsulesPercentage = capsuleProgressList.stream()
                .map(LearnerCapsuleProgress::getProgressPercentage)
                .reduce(0.0, Double::sum);

        // calculate percentage
        double trackPercentage = capsuleProgressList.isEmpty() ? 0.0 : sumOfCapsulesPercentage/capsuleProgressList.size();
        double roundedPercentage = Math.ceil(trackPercentage * 10.0) / 10.0;

        if(roundedPercentage >= 100.0){
            growthTrackProgress.setStatus(ProgressStatus.COMPLETED);
            growthTrackProgress.setCompletedAt(LocalDateTime.now());
            roundedPercentage = 100.0;
        }
        growthTrackProgress.setProgressPercentage(roundedPercentage);

        return growthTrackProgress;
    }

    private LearnerRoadmap calculateRoadmap(LearnerTrackProgress growthTrackProgress){
        LearnerRoadmap learnerRoadmap = growthTrackProgress.getLearnerRoadmap();
        List<LearnerTrackProgress> growthTrackProgressList = learnerRoadmap.getLearnerTrackProgresses();

        // only calculate the overall talent route percentage for growth tracks that are unlocked
        List<LearnerTrackProgress> growthTracksTakenByLearner = growthTrackProgressList.stream()
                .filter(track-> !track.getStatus().equals(ProgressStatus.NOT_STARTED))
                .toList();

        // get the sum of growth track percentages
        double sumOfGrowthTracksPercentage = growthTracksTakenByLearner.stream()
                .map(LearnerTrackProgress::getProgressPercentage)
                .reduce(0.0, Double::sum);

        // calculate percentage
        double learnerRoadmapPercentage = growthTrackProgressList.isEmpty() ? 0.0 : sumOfGrowthTracksPercentage/growthTracksTakenByLearner.size();
        double roundedPercentage = Math.ceil(learnerRoadmapPercentage * 10.0) / 10.0;

        if(roundedPercentage >= 100.0){
            learnerRoadmap.setCompletionDate(LocalDateTime.now());
            roundedPercentage = 100.0;
        }

        learnerRoadmap.setOverallProgressPercentage(roundedPercentage);

        return learnerRoadmap;
    }

    @Transactional
    @Override
    public void recalculateAndUpdateLearnerRoadmap(RecalculateProgressRequestDto dto) {
        LearnerRoadmap talentRoute = learnerRoadmapRepository
                .findByUserIdAndTalentRouteId(dto.getLearnerId(), dto.getTalentRouteId())
                .orElseThrow(() -> new TalentRouteNotFoundException("The learner isn't enrolled in this roadmap"));

        updateLearnerRoadmap(talentRoute); // first update learner roadmap
        recalculateProgress(talentRoute); // recalculate learner progress

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
                        .progressPercentage(0.0)
                        .build();

                talentRoute.getLearnerTrackProgresses().add(learnerGrowthTrackProgress); // add growth track to roadmap

                existingTracks.put(growthTrackId, learnerGrowthTrackProgress); // update learnerTrackProgress
            }
            // add non-existing capsules to the growth track progress
            addNewCapsulesToGrowthTrackProgress(learnerGrowthTrackProgress, growthTrackId);

        }
    }

    private void addNewCapsulesToGrowthTrackProgress(LearnerTrackProgress learnerGrowthTrackProgress, UUID growthTrackId){
        //find all the capsule in the growth track(new one + existing in the growth track progress)
        List<SkillCapsuleSnapshot> allCapsules =
                growthTrackCapsuleMappingRepository.findCapsulesByGrowthTrackId(growthTrackId);

        // find the existing capsules in the growth track progress
        Map<UUID, LearnerCapsuleProgress> existingCapsules = learnerGrowthTrackProgress.getLearnerCapsuleProgresses().stream()
                .collect(Collectors.toMap(cp->cp.getSkillCapsule().getSkillCapsuleId(), cp->cp));

        for(SkillCapsuleSnapshot capsule: allCapsules){
            UUID capsuleId = capsule.getSkillCapsuleId();
            LearnerCapsuleProgress learnerCapsuleProgress = existingCapsules.get(capsuleId);

            if(learnerCapsuleProgress == null){ // add only new the ones aren't in the growth track progress
                learnerCapsuleProgress = LearnerCapsuleProgress.builder()
                        .learnerTrackProgress(learnerGrowthTrackProgress)
                        .skillCapsule(capsule)
                        .status(ProgressStatus.NOT_STARTED)
                        .progressPercentage(0.0)
                        .build();

                // attach the learner capsule progress to growth track progress
                learnerGrowthTrackProgress.getLearnerCapsuleProgresses().add(learnerCapsuleProgress);

                existingCapsules.put(capsuleId, learnerCapsuleProgress); // update learnerCapsule progress
            }

            // add non-existing atom to capsule progress
            addNewAtomsToCapsuleProgress(learnerCapsuleProgress, capsuleId);

        }
    }

    private void addNewAtomsToCapsuleProgress(LearnerCapsuleProgress learnerCapsuleProgress, UUID capsuleId){
        //find all the atoms in the capsule(new one + existing in the capsule progress)
        List<SkillAtomSnapshot> allAtoms =
                capsuleAtomMappingRepository.findAtomsByCapsuleId(capsuleId);

        // find the existing atoms in the capsule progress
        Map<UUID, LearnerAtomProgress> existingAtoms = learnerCapsuleProgress.getLearnerAtomProgresses().stream()
                .collect(Collectors.toMap(cp->cp.getSkillAtom().getSkillAtomId(), cp->cp));

        for(SkillAtomSnapshot atom: allAtoms){
            UUID atomId = atom.getSkillAtomId();
            LearnerAtomProgress learnerAtomProgress = existingAtoms.get(atomId);

            if(learnerAtomProgress == null){ // add only new the ones aren't in the capsule progress
                learnerAtomProgress = LearnerAtomProgress.builder()
                        .learnerCapsuleProgress(learnerCapsuleProgress)
                        .skillAtom(atom)
                        .status(ProgressStatus.NOT_STARTED)
                        .isCompleted(false)
                        .build();

                // attach the learner atom progress to capsule progress
                learnerCapsuleProgress.getLearnerAtomProgresses().add(learnerAtomProgress);

                existingAtoms.put(atomId, learnerAtomProgress); // update learnerAtom progress
            }

        }
    }

    public void recalculateProgress(LearnerRoadmap talentRoute) {
        // Recalculate all growth tracks progresses
        List<LearnerTrackProgress> growthTracks = talentRoute.getLearnerTrackProgresses();
        double totalGrowthTrackPercentage = getTotalGrowthTrackPercentageInTalentRoute(growthTracks);

        // Recalculate talentRoute percentage
        recalculateTalentRoutePercentage(talentRoute, growthTracks, totalGrowthTrackPercentage);

    }

    private void recalculateTalentRoutePercentage(LearnerRoadmap talentRoute,
                                                  List<LearnerTrackProgress> growthTracks,
                                                  double totalGrowthTrackPercentage ){

        // only calculate the overall talent route percentage for growth tracks that are unlocked
        List<LearnerTrackProgress> growthTracksTakenByLearner = growthTracks.stream()
                .filter(track-> !track.getStatus().equals(ProgressStatus.NOT_STARTED))
                .toList();
        double talentRoutePercentage = growthTracksTakenByLearner.isEmpty() ? 0.0 : totalGrowthTrackPercentage / growthTracksTakenByLearner.size();
        double roundedPercentage = Math.ceil(talentRoutePercentage * 10.0) / 10.0;
        talentRoute.setOverallProgressPercentage(roundedPercentage);

        if (roundedPercentage == 0.0) {
            talentRoute.setEnrollmentStatus(EnrollmentStatus.ACTIVE); // still enrolled, not started
        } else if (roundedPercentage >= 100.0) {
            talentRoute.setEnrollmentStatus(EnrollmentStatus.COMPLETED);
            roundedPercentage = 100.0;
        }
        talentRoute.setOverallProgressPercentage(roundedPercentage);

        learnerRoadmapRepository.save(talentRoute); // store roadmap updated progress in the Database
    }

    private double getTotalGrowthTrackPercentageInTalentRoute(List<LearnerTrackProgress> growthTracks){
        double totalTrackPercentage = 0.0; // this is required to update roadmap overall percentage

        // loop throw each growth track progress to recalculate percentage and the get the new percentage
        for (LearnerTrackProgress growthTrackProgress : growthTracks) {
            // first get the total percentage of capsule to calculate the growth track percentage
            List<LearnerCapsuleProgress> capsules = growthTrackProgress.getLearnerCapsuleProgresses();
            double totalCapsulePercentage = getTotalCapsulePercentageInGrowthTrack(capsules);

            // update current growth track progress information
            double currentGrowthTrackPercentage = capsules.isEmpty() ? 0.0 : totalCapsulePercentage / capsules.size();
            double roundedPercentage = Math.ceil(currentGrowthTrackPercentage * 10.0) / 10.0;
            growthTrackProgress.setProgressPercentage(roundedPercentage);

            // set growth track status
            if(roundedPercentage == 0.0){
                growthTrackProgress.setStatus(ProgressStatus.NOT_STARTED);
            } else if (roundedPercentage >= 100.0) {
                growthTrackProgress.setStatus(ProgressStatus.COMPLETED);
                roundedPercentage = 100.0;
            }else{
                growthTrackProgress.setStatus(ProgressStatus.IN_PROGRESS);
            }

            totalTrackPercentage += roundedPercentage;
        }
        return totalTrackPercentage;
    }

    private double getTotalCapsulePercentageInGrowthTrack(List<LearnerCapsuleProgress> capsules){
        double totalCapsulePercentage = 0.0; // this is required to calculate the growth track percentage

        // loop throw each capsule progress to recalculate percentage and the get the new percentage
        for (LearnerCapsuleProgress capsuleProgress : capsules) {
            // first calculate the atoms completion
            List<LearnerAtomProgress> atoms = capsuleProgress.getLearnerAtomProgresses();

            int completedAtoms = (int) atoms.stream()
                    .filter(LearnerAtomProgress::isCompleted)
                    .count();

            double currentCapsulePercentage = atoms.isEmpty() ? 0.0 :
                    (completedAtoms * 100.0) / atoms.size();

            capsuleProgress.setProgressPercentage(currentCapsulePercentage);

            // update current capsule progress information
            if(currentCapsulePercentage == 0.0){
                capsuleProgress.setStatus(ProgressStatus.NOT_STARTED);
            } else if (currentCapsulePercentage >= 100.0) {
                capsuleProgress.setStatus(ProgressStatus.COMPLETED);
                currentCapsulePercentage = 100.0;
            }else{
                capsuleProgress.setStatus(ProgressStatus.IN_PROGRESS);
            }

            totalCapsulePercentage += currentCapsulePercentage;
        }
        return totalCapsulePercentage;
    }
}
