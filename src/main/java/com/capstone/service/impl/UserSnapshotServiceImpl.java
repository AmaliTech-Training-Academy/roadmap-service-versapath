package com.capstone.service.impl;

import com.capstone.exception.UserProcessingException;
import com.capstone.mapper.UserEventMapper;
import com.capstone.model.UserSnapshot;
import com.capstone.repository.UserSnapshotRepository;
import com.capstone.service.UserSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.ProduceUserEvent;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserSnapshotServiceImpl implements UserSnapshotService {

    private final UserSnapshotRepository userSnapshotRepository;
    private final UserEventMapper userEventMapper;

    @Override
    public UserSnapshot processUserEvent(ProduceUserEvent event) {
        log.info("Processing user event for userId: {}", event.getVersapathUserId());

        try {
            Optional<UserSnapshot> existingUser = userSnapshotRepository.findByUserId(event.getVersapathUserId());

            if (existingUser.isPresent()) {
                log.info("User exists, updating user with ID: {}", event.getVersapathUserId());
                return updateUser(existingUser.get(), event);
            } else {
                log.info("User does not exist, creating new user with ID: {}", event.getVersapathUserId());
                return createUser(event);
            }

        } catch (Exception e) {
            log.error("Error processing user event for userId: {}", event.getVersapathUserId(), e);
            throw new UserProcessingException("Failed to process user event", e);
        }
    }

    @Override
    public UserSnapshot createUser(ProduceUserEvent event) {
        log.debug("Creating new user from event: {}", event);

        try {
            UserSnapshot newUser = userEventMapper.toUserSnapshot(event);
            UserSnapshot savedUser = userSnapshotRepository.save(newUser);
            log.info("Successfully created user with ID: {}", savedUser.getUserId());
            return savedUser;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when creating user with ID: {}", event.getVersapathUserId(), e);
            throw new UserProcessingException("User creation failed due to data constraint violation", e);
        } catch (Exception e) {
            log.error("Unexpected error creating user with ID: {}", event.getVersapathUserId(), e);
            throw new UserProcessingException("Failed to create user", e);
        }
    }

    @Override
    public UserSnapshot updateUser(UserSnapshot existingUser, ProduceUserEvent event) {
        log.debug("Updating existing user {} with event data: {}", existingUser.getUserId(), event);

        try {
            userEventMapper.updateUserSnapshot(event, existingUser);
            UserSnapshot updatedUser = userSnapshotRepository.save(existingUser);
            log.info("Successfully updated user with ID: {}", updatedUser.getUserId());
            return updatedUser;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when updating user with ID: {}", existingUser.getUserId(), e);
            throw new UserProcessingException("User update failed due to data constraint violation", e);
        } catch (Exception e) {
            log.error("Unexpected error updating user with ID: {}", existingUser.getUserId(), e);
            throw new UserProcessingException("Failed to update user", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserSnapshot> findByUserId(UUID userId) {
        log.debug("Finding user by userId: {}", userId);
        return userSnapshotRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserId(UUID userId) {
        log.debug("Checking if user exists by userId: {}", userId);
        return userSnapshotRepository.existsByUserId(userId);
    }
}
