package com.capstone.service.impl;

import com.capstone.dto.request.LearnerOnboardingRequestDto;
import com.capstone.dto.request.RoadmapRequestDto;
import com.capstone.dto.response.LearnerOnboardingResponseDto;
import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.exception.*;
import com.capstone.mapper.LearnerOnboardingMapper;
import com.capstone.messaging.KafkaProducer;
import com.capstone.model.LearnerOnboarding;
import com.capstone.model.PaginationMetadata;
import com.capstone.model.UserSnapshot;
import com.capstone.model.TalentRouteSnapshot;
import com.capstone.model.GrowthTrackSnapshot;
import com.capstone.repository.LearnerOnboardingRepository;
import com.capstone.repository.UserSnapshotRepository;
import com.capstone.repository.TalentRouteSnapshotRepository;
import com.capstone.repository.GrowthTrackSnapshotRepository;
import com.capstone.service.LearnerOnboardingService;
import com.capstone.service.LearnerRoadmapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.LearnerOnBoardingEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LearnerOnboardingServiceImpl implements LearnerOnboardingService {

    private final LearnerOnboardingRepository learnerOnboardingRepository;
    private final UserSnapshotRepository userSnapshotRepository;
    private final TalentRouteSnapshotRepository talentRouteSnapshotRepository;
    private final GrowthTrackSnapshotRepository growthTrackSnapshotRepository;
    private final LearnerOnboardingMapper learnerOnboardingMapper;
    private final LearnerRoadmapService learnerRoadmapService;
    private final KafkaProducer kafkaProducer;

    @Override
    @Transactional
    public LearnerOnboardingResponseDto createOnboarding(LearnerOnboardingRequestDto requestDto) {
        log.info("Creating learner onboarding for learner: {}", requestDto.getLearnerId());

        // Validate and get required entities
        UserSnapshot learner = userSnapshotRepository.findByUserId(requestDto.getLearnerId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + requestDto.getLearnerId()));

        TalentRouteSnapshot talentRoute = talentRouteSnapshotRepository.findByTalentRouteId(requestDto.getTalentRouteId())
                .orElseThrow(() -> new TalentRouteNotFoundException("Talent route not found with ID: " + requestDto.getTalentRouteId()));

        GrowthTrackSnapshot growthTrack = growthTrackSnapshotRepository.findByGrowthTrackId(requestDto.getGrowthTrackId())
                .orElseThrow(() -> new GrowthTrackNotFoundException("Growth track not found with ID: " + requestDto.getGrowthTrackId()));

        // Check if onboarding already exists for this learner
        if (existsByLearnerId(requestDto.getLearnerId())) {
            throw new ValidationException("Onboarding already exists for learner: " + requestDto.getLearnerId());
        }

        // Create entity and set relationships
        LearnerOnboarding onboarding = learnerOnboardingMapper.toEntity(requestDto);
        onboarding.setLearner(learner);
        onboarding.setTalentRoute(talentRoute);
        onboarding.setGrowthTrack(growthTrack);

        LearnerOnboarding savedOnboarding = learnerOnboardingRepository.save(onboarding);

        // Assign learner to talent route (creates roadmap, enrolls in growth track, etc.)
        RoadmapRequestDto roadmapDto =  RoadmapRequestDto.builder()
                .learnerId(savedOnboarding.getLearner().getUserId())
                .talentRouteId(savedOnboarding.getTalentRoute().getTalentRouteId())
                .build();
        learnerRoadmapService.assignLearnerToTalentRoute(roadmapDto);

        LearnerOnboardingResponseDto responseDto = learnerOnboardingMapper.toBasicResponseDto(savedOnboarding);

        log.info("Successfully created learner onboarding with ID: {}", savedOnboarding.getId());

        try {
            LearnerOnBoardingEvent learnerEvent = LearnerOnBoardingEvent.builder()
                    .learnerId(savedOnboarding.getLearner().getUserId())
                    .requiresOnboarding(false)
                    .build();
            kafkaProducer.produce(learnerEvent);
            log.info("Successfully published learner event: {} ", savedOnboarding.getLearner().getUserId());
        } catch (Exception eventException) {
            log.error("Failed to publish Learner event for LEARNER user: {}", savedOnboarding.getLearner().getUserId(), eventException);
            throw new EventPublishingException("Failed to publish user event for Onboard completion", eventException);
        }

        return responseDto;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<LearnerOnboardingResponseDto> findAll(Pageable pageable) {
        Page<LearnerOnboarding> onboardingsPage = learnerOnboardingRepository.findAll(pageable);

        // Use basic mapping (no names) for performance in list view
        List<LearnerOnboardingResponseDto> responseDto = learnerOnboardingMapper
                .toBasicResponseDtoList(onboardingsPage.getContent());

        PaginationMetadata metadata = PaginationMetadata.builder()
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(onboardingsPage.getTotalElements())
                .totalPages(onboardingsPage.getTotalPages())
                .hasNext(onboardingsPage.isFirst())
                .hasPrevious(onboardingsPage.isLast())
                .build();

        return PaginatedResponseDto.<LearnerOnboardingResponseDto>builder()
                .items(responseDto)
                .pagination(metadata)
                .build();
    }

    @Override
    public boolean existsByLearnerId(UUID learnerId) {
        return learnerOnboardingRepository.existsByLearnerId(learnerId);
    }
}
