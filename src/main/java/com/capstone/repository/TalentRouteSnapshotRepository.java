package com.capstone.repository;

import com.capstone.model.TalentRouteSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TalentRouteSnapshotRepository extends JpaRepository<TalentRouteSnapshot, UUID> {
    Optional<TalentRouteSnapshot> findByTalentRouteId(UUID talentRouteId);
}
