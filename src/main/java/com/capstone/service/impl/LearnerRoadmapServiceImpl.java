package com.capstone.service.impl;

import com.capstone.dto.request.RoadmapRequestDto;
import com.capstone.dto.response.LearnerAtomProgressDto;
import com.capstone.dto.response.LearnerCapsuleProgressDto;
import com.capstone.dto.response.LearnerRoadmapWithProgressDto;
import com.capstone.dto.response.LearnerTrackProgressDto;
import com.capstone.exception.*;
import com.capstone.mapper.LearnerRoadmapViewMapper;
import com.capstone.messaging.KafkaProducer;
import com.capstone.model.*;
import com.capstone.repository.*;
import com.capstone.service.LearnerRoadmapService;
import lombok.extern.slf4j.Slf4j;
import org.common.event.LearnerOnBoardingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearnerRoadmapServiceImpl implements LearnerRoadmapService {
    private final LearnerRoadmapRepository learnerRoadmapRepository;
    private final TalentRouteSnapshotRepository talentRouteSnapshotRepository;
    private final UserSnapshotRepository userSnapshotRepository;
    private final RouteTrackMappingRepository routeTrackMappingRepository;
    private final GrowthTrackCapsuleMappingRepository growthTrackCapsuleMappingRepository;
    private final CapsuleAtomMappingRepository capsuleAtomMappingRepository;

    // New dependencies for learner view methods
    private final LearnerTrackProgressRepository learnerTrackProgressRepository;
    private final LearnerCapsuleProgressRepository learnerCapsuleProgressRepository;
    private final LearnerRoadmapViewMapper mapper;
    private final MentorRouteMappingRepository mentorRouteMappingRepository;
    private final MentorLearnerMappingRepository mentorLearnerMappingRepository;
    private final MentorSnapshotRepository mentorSnapshotRepository;
    private final KafkaProducer kafkaProducer;


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

        assignLearnerToMentor(roadmapRequestDto);

        try {
            LearnerOnBoardingEvent learnerEvent = LearnerOnBoardingEvent.builder()
                    .learnerId(learnerRoadmap.getUserId())
                    .requiresOnboarding(false)
                    .build();
            kafkaProducer.produce(learnerEvent);
            log.info("Successfully published learner event: {} ", learnerRoadmap.getUserId());
        } catch (Exception eventException) {
            log.error("Failed to publish Learner event for LEARNER user: {}", learnerRoadmap.getUserId(), eventException);
            throw new EventPublishingException("Failed to publish user event for Onboard completion", eventException);
        }
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

        List<LearnerRoadmap> learnerRoadmapList = learnerRoadmapRepository.findLearnerRoadmapByUserIdANDTalentRouteId(learnerId, talentRouteId);
        // find an ongoing learner roadmap
        if(!learnerRoadmapList.isEmpty()){
            long uncompletedRoadmapNumber = learnerRoadmapList.stream()
                    .filter(roadmap->roadmap.getOverallProgressPercentage() < 100.0)
                    .count();
            if(uncompletedRoadmapNumber >= 1){
                throw new RoadmapExistException("You have an ongoing roadmap, you cannot start a new one!");
            }
        }

        return LearnerRoadmap.builder()
                .talentRoute(talentRoute)
                .userId(learnerId)
                .enrollmentStatus(EnrollmentStatus.ACTIVE)
                .overallProgressPercentage(0.0)
                .build();
    }

    private List<LearnerTrackProgress> createGrowthTrackProgresses(List<GrowthTrackSnapshot> growthTracks, LearnerRoadmap learnerRoadmap){
        return growthTracks.stream()
                .map(track -> {
                    LearnerTrackProgress growthTrackProgress = LearnerTrackProgress.builder()
                            .learnerRoadmap(learnerRoadmap)
                            .growthTrack(track)
                            .status(ProgressStatus.NOT_STARTED)
                            .progressPercentage(0.0)
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
                        .progressPercentage(0.0)
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

    // Learner view methods implementation
    @Override
    public Optional<LearnerRoadmapWithProgressDto> getLearnerRoadmap(UUID learnerId) {
        return learnerRoadmapRepository.findActiveRoadmapByUserId(learnerId)
            .map(mapper::toLearnerRoadmapDto);
    }

    @Override
    public List<LearnerTrackProgressDto> getLearnerTracks(UUID learnerId) {
        // Validate learner has active roadmap first
        if (learnerRoadmapRepository.findActiveRoadmapByUserId(learnerId).isPresent()) {
            List<Object[]> trackProgressesWithSequence =
                    learnerTrackProgressRepository.findTrackProgressesByLearnerIdWithSequence(learnerId);

            return trackProgressesWithSequence.stream()
                .map(result -> {
                    LearnerTrackProgress trackProgress = (LearnerTrackProgress) result[0];
                    Integer sequenceOrder = (Integer) result[1];

                    LearnerTrackProgressDto dto = mapper.toLearnerTrackProgressDto(trackProgress);
                    dto.setSequenceOrder(sequenceOrder);

                    // Set unlock status: first track is always unlocked, others depend on previous completion
                    dto.setIsUnlocked(sequenceOrder == 1 || isPreviousTrackCompleted(learnerId, sequenceOrder));

                    return dto;
                })
                .toList();
        } else {
            throw new RoadmapNotFoundException("No active roadmap found for learner: " + learnerId);
        }
    }

    @Override
    public List<LearnerCapsuleProgressDto> getTrackCapsules(UUID learnerId, UUID trackId) {
        // Validate learner has active roadmap
        if (learnerRoadmapRepository.findActiveRoadmapByUserId(learnerId).isEmpty()) {
            throw new RoadmapNotFoundException("No active roadmap found for learner: " + learnerId);
        }

        List<Object[]> capsuleProgressesWithSequence =
            learnerTrackProgressRepository.findCapsuleProgressesByLearnerIdAndTrackIdWithSequence(learnerId, trackId);

        // If empty, track might not exist or not belong to learner
        if (capsuleProgressesWithSequence.isEmpty()) {
            throw new GrowthTrackNotFoundException("Track not found or not accessible for learner", trackId);
        }

        return capsuleProgressesWithSequence.stream()
            .map(result -> {
                LearnerCapsuleProgress capsuleProgress = (LearnerCapsuleProgress) result[0];
                Integer sequenceOrder = (Integer) result[1];

                LearnerCapsuleProgressDto dto = mapper.toLearnerCapsuleProgressDto(capsuleProgress);
                dto.setSequenceOrder(sequenceOrder);

                // Set unlock status: first capsule is always unlocked, others depend on previous completion
                dto.setIsUnlocked(sequenceOrder == 1 || isPreviousCapsuleCompleted(learnerId, trackId, sequenceOrder));

                return dto;
            })
            .toList();
    }

    @Override
    public List<LearnerAtomProgressDto> getCapsuleAtoms(UUID learnerId, UUID capsuleId) {
        // Validate learner has active roadmap
        if (learnerRoadmapRepository.findActiveRoadmapByUserId(learnerId).isEmpty()) {
            throw new RoadmapNotFoundException("No active roadmap found for learner: " + learnerId);
        }

        List<Object[]> atomProgressesWithSequence =
            learnerCapsuleProgressRepository.findAtomProgressesByLearnerIdAndCapsuleIdWithSequence(learnerId, capsuleId);

        // If empty, capsule might not exist or not belong to learner
        if (atomProgressesWithSequence.isEmpty()) {
            throw new SkillCapsuleNotFoundException("Capsule not found or not accessible for learner", capsuleId);
        }

        return atomProgressesWithSequence.stream()
            .map(result -> {
                LearnerAtomProgress atomProgress = (LearnerAtomProgress) result[0];
                Integer sequenceOrder = (Integer) result[1];

                LearnerAtomProgressDto dto = mapper.toLearnerAtomProgressDto(atomProgress);

                // Set unlock status: first atom is always unlocked, others depend on previous completion
                dto.setIsUnlocked(sequenceOrder == 1 || isPreviousAtomCompleted(learnerId, capsuleId, sequenceOrder));

                return dto;
            })
            .toList();
    }

    // Helper methods for unlock logic
    private boolean isPreviousTrackCompleted(UUID learnerId, Integer currentSequence) {
        if (currentSequence <= 1) return true;

        List<Object[]> allTracks = learnerTrackProgressRepository.findTrackProgressesByLearnerIdWithSequence(learnerId);

        return allTracks.stream()
            .filter(result -> result[1].equals(currentSequence - 1))
            .findFirst()
            .map(result -> {
                LearnerTrackProgress previousTrack = (LearnerTrackProgress) result[0];
                return previousTrack.getStatus() == ProgressStatus.COMPLETED;
            })
            .orElse(false);
    }

    private boolean isPreviousCapsuleCompleted(UUID learnerId, UUID trackId, Integer currentSequence) {
        if (currentSequence <= 1) return true;

        List<Object[]> allCapsules = learnerTrackProgressRepository
            .findCapsuleProgressesByLearnerIdAndTrackIdWithSequence(learnerId, trackId);

        return allCapsules.stream()
            .filter(result -> result[1].equals(currentSequence - 1))
            .findFirst()
            .map(result -> {
                LearnerCapsuleProgress previousCapsule = (LearnerCapsuleProgress) result[0];
                return previousCapsule.getStatus() == ProgressStatus.COMPLETED;
            })
            .orElse(false);
    }

    private boolean isPreviousAtomCompleted(UUID learnerId, UUID capsuleId, Integer currentSequence) {
        if (currentSequence <= 1) return true;

        List<Object[]> allAtoms = learnerCapsuleProgressRepository
            .findAtomProgressesByLearnerIdAndCapsuleIdWithSequence(learnerId, capsuleId);

        return allAtoms.stream()
            .filter(result -> result[1].equals(currentSequence - 1))
            .findFirst()
            .map(result -> {
                LearnerAtomProgress previousAtom = (LearnerAtomProgress) result[0];
                return previousAtom.isCompleted();
            })
            .orElse(false);
    }

    private void assignLearnerToMentor(RoadmapRequestDto roadmapRequestDto){
        MentorSnapshot mentor = selectMentorToAssign(roadmapRequestDto);
        UserSnapshot learner = userSnapshotRepository.findByUserId(roadmapRequestDto.getLearnerId())
                .orElseThrow( () -> new UserNotFoundException("A learner provided doesn't exist")
                );

        // assign learner to a mentor
        MentorLearnerMapping mentorLearnerMapping = MentorLearnerMapping.builder()
                        .mentor(mentor)
                        .learner(learner)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

        mentorLearnerMappingRepository.save(mentorLearnerMapping);

        // update number of learners assigned to mentor
        mentorSnapshotRepository.incrementAssignedLearner(mentor.getId());

        mentorSnapshotRepository.save(mentor);

    }

    private MentorSnapshot selectMentorToAssign(RoadmapRequestDto roadmapRequestDto){
        List<MentorSnapshot> mentors = mentorRouteMappingRepository.findMentorsByTalentRouteId(roadmapRequestDto.getTalentRouteId());

        if (mentors.isEmpty()) {
            throw new MentorNotAvailableException("The Talent route provided has no mentor assigned to");
        }

        int minLearnerNumber = mentors.stream()
                .mapToInt(MentorSnapshot::getAssignedLearner)
                .min()
                .getAsInt();

        // Get all mentors with the same minimum number learners
        List<MentorSnapshot> mentorsWithMinLearners = mentors.stream()
                .filter(mentor -> mentor.getAssignedLearner() == minLearnerNumber)
                .toList();

        // select the first mentor
        return mentorsWithMinLearners.getFirst();
    }
}
