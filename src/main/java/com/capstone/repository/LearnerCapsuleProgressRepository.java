package com.capstone.repository;

import com.capstone.model.LearnerCapsuleProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LearnerCapsuleProgressRepository extends JpaRepository<LearnerCapsuleProgress, UUID> {
}
