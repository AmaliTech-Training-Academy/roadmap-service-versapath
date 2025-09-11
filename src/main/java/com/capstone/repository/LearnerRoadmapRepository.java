package com.capstone.repository;

import com.capstone.model.LearnerRoadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LearnerRoadmapRepository extends JpaRepository<LearnerRoadmap, UUID> {
}
