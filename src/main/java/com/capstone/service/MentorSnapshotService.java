package com.capstone.service;

import com.capstone.dto.response.LearnerDto;
import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.dto.response.MentorResponseDto;
import com.capstone.model.MentorSnapshot;
import org.common.event.ProduceMentorEvent;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MentorSnapshotService {
    MentorSnapshot processMentorEvent(ProduceMentorEvent event);
    MentorSnapshot createMentor(ProduceMentorEvent event);
    MentorSnapshot updateMentor(MentorSnapshot existingMentor, ProduceMentorEvent event);
    void smartUpdateMentorRouteMappings(MentorSnapshot mentor, List<UUID> specializationRouteIds);
    MentorSnapshot assignSpecializationsToMentor(ProduceMentorEvent event);

    // Query methods
    PaginatedResponseDto<MentorResponseDto> findAllBasic(Pageable pageable);
    PaginatedResponseDto<MentorResponseDto> findAllWithSpecializations(Pageable pageable);
    PaginatedResponseDto<MentorResponseDto> searchByNameBasic(String name, Pageable pageable);
    Optional<MentorResponseDto> findByMentorIdBasic(UUID mentorId);
    Optional<MentorResponseDto> findByMentorIdWithSpecializations(UUID mentorId);
    PaginatedResponseDto<MentorResponseDto> findBySpecialization(UUID talentRouteId, Pageable pageable);
    PaginatedResponseDto<LearnerDto> findLearnersByMentorId(UUID mentorId, Pageable pageable);
}
