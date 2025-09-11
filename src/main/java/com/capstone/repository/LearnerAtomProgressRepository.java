package com.capstone.repository;

import com.capstone.model.LearnerAtomProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LearnerAtomProgressRepository extends JpaRepository<LearnerAtomProgress, UUID> {
}
