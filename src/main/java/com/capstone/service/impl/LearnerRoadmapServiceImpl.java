package com.capstone.service.impl;

import com.capstone.dto.roadmap.RoadmapRequestDto;
import com.capstone.exception.RoadmapExistException;
import com.capstone.exception.TalentRouteNotFoundException;
import com.capstone.exception.UserNotFoundException;
import com.capstone.model.*;
import com.capstone.repository.*;
import com.capstone.service.LearnerRoadmapService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LearnerRoadmapServiceImpl implements LearnerRoadmapService {
    private final LearnerRoadmapRepository learnerRoadmapRepository;
    private final TalentRouteSnapshotRepository talentRouteSnapshotRepository;
    private final UserSnapshotRepository userSnapshotRepository;
    private final RouteTrackMappingRepository routeTrackMappingRepository;
    private final GrowthTrackCapsuleMappingRepository growthTrackCapsuleMappingRepository;
    private final CapsuleAtomMappingRepository capsuleAtomMappingRepository;

    @Transactional
    @Override
    public void assignLearnerToTalentRoute(RoadmapRequestDto roadmapRequestDto) {

        LearnerRoadmap learnerRoadmap = createLearnerRoadmap(roadmapRequestDto.getLearnerId(),
                roadmapRequestDto.getTalentRouteId());

        // fetch all the growth tracks belong to the talent route
        List<GrowthTrackSnapshot> growthTracks = routeTrackMappingRepository
                .findGrowthTracksByTalentRouteId(roadmapRequestDto.getTalentRouteId());

        List<LearnerTrackProgress> growthTrackProgresses = createGrowthTrackProgresses(growthTracks, learnerRoadmap);

        learnerRoadmap.setLearnerTrackProgresses(growthTrackProgresses); // map roadmap to growth track progress

        learnerRoadmapRepository.save(learnerRoadmap);
    }

    private LearnerRoadmap createLearnerRoadmap(UUID learnerId, UUID talentRouteId){
        TalentRouteSnapshot talentRoute = talentRouteSnapshotRepository.findByTalentRouteId(talentRouteId)
                .orElseThrow( () -> new TalentRouteNotFoundException("A talent route provided doesn't exist")
                );

        if(userSnapshotRepository.findByUserId(learnerId).isEmpty()){
            throw new UserNotFoundException("A learner provided doesn't exist");
        }

        if(learnerRoadmapRepository.findByUserIdAndTalentRouteId(learnerId, talentRouteId).isPresent()){
            throw new RoadmapExistException("You are already enrolled in this roadmap");
        }

        return LearnerRoadmap.builder()
                .talentRoute(talentRoute)
                .userId(learnerId)
                .enrollmentStatus(EnrollmentStatus.ACTIVE)
                .overallProgressPercentage(0)
                .build();
    }

    private List<LearnerTrackProgress> createGrowthTrackProgresses(List<GrowthTrackSnapshot> growthTracks, LearnerRoadmap learnerRoadmap){
        return growthTracks.stream()
                .map(track -> {
                    LearnerTrackProgress growthTrackProgress = LearnerTrackProgress.builder()
                            .learnerRoadmap(learnerRoadmap)
                            .growthTrack(track)
                            .status(ProgressStatus.NOT_STARTED)
                            .progressPercentage(0)
                            .build();

                    // create immediately capsule progress for this growth track
                    List<LearnerCapsuleProgress> capsuleProgresses = createCapsuleProgresses(growthTrackProgress, track);

                    // map capsule progress to the growth track
                    growthTrackProgress.setLearnerCapsuleProgresses(capsuleProgresses);

                    return growthTrackProgress;
                }
                )
                .toList();
    }

    private List<LearnerCapsuleProgress> createCapsuleProgresses(LearnerTrackProgress learnerTrackProgress,
                                                                 GrowthTrackSnapshot growthTrack){
        // fetch all the capsules belong to the growth track
        List<SkillCapsuleSnapshot> capsules = growthTrackCapsuleMappingRepository
                .findCapsulesByGrowthTrackId(growthTrack.getGrowthTrackId());

        // initialize progresses
        return capsules.stream()
                .map(capsule -> {

                    LearnerCapsuleProgress learnerCapsuleProgress = LearnerCapsuleProgress.builder()
                        .learnerTrackProgress(learnerTrackProgress)
                        .skillCapsule(capsule)
                        .status(ProgressStatus.NOT_STARTED)
                        .progressPercentage(0)
                        .build();

                    // create atom progress immediately for this capsule
                    List<LearnerAtomProgress> atomProgresses = createAtomProgresses(learnerCapsuleProgress, capsule);

                    //map atom progress to the skill capsule progress
                    learnerCapsuleProgress.setLearnerAtomProgresses(atomProgresses);

                    return learnerCapsuleProgress;
                }
                )
                .toList();

    }

    private List<LearnerAtomProgress> createAtomProgresses(LearnerCapsuleProgress learnerCapsuleProgress,
                                                                 SkillCapsuleSnapshot capsule){
        // fetch all the atoms belong to the capsule
        List<SkillAtomSnapshot> atoms = capsuleAtomMappingRepository
                .findAtomsByCapsuleId(capsule.getSkillCapsuleId());

        // initialize progresses
        return atoms.stream()
                .map(atom -> LearnerAtomProgress.builder()
                        .learnerCapsuleProgress(learnerCapsuleProgress)
                        .skillAtom(atom)
                        .status(ProgressStatus.NOT_STARTED)
                        .build()
                )
                .toList();

    }
}
