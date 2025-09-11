package com.capstone.repository;

import com.capstone.model.UserSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserSnapshotRepository extends JpaRepository<UserSnapshot, UUID> {
}
