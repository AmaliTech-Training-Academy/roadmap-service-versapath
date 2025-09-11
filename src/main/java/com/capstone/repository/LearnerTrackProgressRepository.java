package com.capstone.repository;

import com.capstone.model.LearnerTrackProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LearnerTrackProgressRepository extends JpaRepository<LearnerTrackProgress, UUID> {
}
