package com.capstone.repository;

import com.capstone.model.MentorLearnerMapping;
import com.capstone.model.UserSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Repository
public interface MentorLearnerMappingRepository extends JpaRepository<MentorLearnerMapping, UUID> {
    @Query("""
        SELECT ml.learner
        FROM MentorLearnerMapping ml
        WHERE ml.mentor.mentorId = :mentorId
    """)
    Page<UserSnapshot> findLearnersByMentorId(@Param("mentorId") UUID mentorId, Pageable pageable);

}
