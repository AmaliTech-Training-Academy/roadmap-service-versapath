package com.capstone.repository;

import com.capstone.model.MentorLearnerMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MentorLearnerMappingRepository extends JpaRepository<MentorLearnerMapping, UUID> {
}
