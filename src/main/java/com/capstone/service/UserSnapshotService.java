package com.capstone.service;

import com.capstone.model.UserSnapshot;
import org.common.event.ProduceUserEvent;

import java.util.Optional;
import java.util.UUID;

public interface UserSnapshotService {

    /**
     * Process user event from Kafka - creates new user or updates existing one
     * @param event the user event from Kafka
     * @return the processed UserSnapshot
     */
    UserSnapshot processUserEvent(ProduceUserEvent event);

    UserSnapshot createUser(ProduceUserEvent event);

    UserSnapshot updateUser(UserSnapshot existingUser, ProduceUserEvent event);

    Optional<UserSnapshot> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
