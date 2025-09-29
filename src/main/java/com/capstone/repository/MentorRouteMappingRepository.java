package com.capstone.repository;

import com.capstone.model.MentorRouteMapping;
import com.capstone.model.MentorSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MentorRouteMappingRepository extends JpaRepository<MentorRouteMapping, UUID> {
    // find all the mentors whose specialization aligns with the talent route
    @Query("""
    SELECT m.mentorSnapshot
    FROM MentorRouteMapping m
    WHERE m.talentRoute.talentRouteId = :talentRouteId
    """)
    List<MentorSnapshot> findMentorsByTalentRouteId(@Param("talentRouteId") UUID talentRouteId);

}
