package com.capstone.repository;

import com.capstone.model.LearnerOnboarding;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LearnerOnboardingRepository extends JpaRepository<LearnerOnboarding, UUID> {

    // Check if onboarding exists for given learnerId
    @Query("SELECT COUNT(lo) > 0 FROM LearnerOnboarding lo WHERE lo.learner.userId = :learnerId")
    boolean existsByLearnerId(@Param("learnerId") UUID learnerId);

    @Query("SELECT lo FROM LearnerOnboarding lo " +
            "LEFT JOIN FETCH lo.learner " +
            "LEFT JOIN FETCH lo.talentRoute " +
            "LEFT JOIN FETCH lo.growthTrack")
    Page<LearnerOnboarding> findAllWithRelationships(Pageable pageable);

}
