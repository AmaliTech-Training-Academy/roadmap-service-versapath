package com.capstone.repository;

import com.capstone.model.MentorSnapshot;
import com.capstone.model.UserSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MentorSnapshotRepository extends JpaRepository<MentorSnapshot, UUID> {

    Optional<MentorSnapshot> findByMentorId(UUID mentorId);

    boolean existsByMentorId(UUID mentorId);

    @Query("SELECT DISTINCT m FROM MentorSnapshot m " +
            "LEFT JOIN FETCH m.mentorRouteMappings mrm " +
            "LEFT JOIN FETCH mrm.talentRoute " +
            "WHERE m.mentorId = :mentorId")
    Optional<MentorSnapshot> findByMentorIdWithRouteMappings(@Param("mentorId") UUID mentorId);

    @Query("SELECT DISTINCT m FROM MentorSnapshot m " +
            "LEFT JOIN FETCH m.mentorRouteMappings mrm " +
            "LEFT JOIN FETCH mrm.talentRoute")
    Page<MentorSnapshot> findAllWithRouteMappings(Pageable pageable);

    @Query("SELECT m FROM MentorSnapshot m " +
            "WHERE LOWER(m.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "OR LOWER(m.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<MentorSnapshot> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    @Query("SELECT m FROM MentorSnapshot m " +
            "JOIN m.mentorRouteMappings mrm " +
            "WHERE mrm.talentRoute.talentRouteId = :talentRouteId")
    Page<MentorSnapshot> findBySpecializationTalentRouteId(@Param("talentRouteId") UUID talentRouteId, Pageable pageable);

    @Modifying
    @Query("""
        UPDATE MentorSnapshot m
        SET m.assignedLearner = m.assignedLearner + 1
        WHERE m.id = :mentorId
    """)
    void incrementAssignedLearner(@Param("mentorId") UUID mentorId);
}
