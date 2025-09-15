package com.capstone.exception;

import java.util.UUID;

public class TrackCapsuleMappingException extends RuntimeException {

    public TrackCapsuleMappingException(String message) {
        super(message);
    }

    public TrackCapsuleMappingException(String message, Throwable cause) {
        super(message, cause);
    }

    public TrackCapsuleMappingException(UUID trackId, UUID capsuleId, String reason) {
        super("Failed to map capsule " + capsuleId + " to track " + trackId + ": " + reason);
    }

    public TrackCapsuleMappingException(UUID trackId, Integer sequenceOrder, String reason) {
        super("Failed to process sequence " + sequenceOrder + " for track " + trackId + ": " + reason);
    }
}
