package com.capstone.exception;

import java.util.UUID;

public class GrowthTrackNotFoundException extends RuntimeException {

    public GrowthTrackNotFoundException(String message) {
        super(message);
    }

    public GrowthTrackNotFoundException(UUID growthTrackId) {
        super("Growth track not found with ID: " + growthTrackId);
    }

    public GrowthTrackNotFoundException(String field, String value) {
        super("Growth track not found with " + field + ": " + value);
    }

    public GrowthTrackNotFoundException(String field, Object value) {
        super("Growth track not found with " + field + ": " + value);
    }
}
