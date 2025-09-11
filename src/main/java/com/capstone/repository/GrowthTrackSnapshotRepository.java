package com.capstone.repository;

import com.capstone.model.GrowthTrackSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GrowthTrackSnapshotRepository extends JpaRepository<GrowthTrackSnapshot, UUID> {
}
