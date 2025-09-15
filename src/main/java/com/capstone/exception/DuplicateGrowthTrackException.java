package com.capstone.exception;

import java.util.UUID;

public class DuplicateGrowthTrackException extends RuntimeException {

    public DuplicateGrowthTrackException(String message) {
        super(message);
    }

    public DuplicateGrowthTrackException(UUID growthTrackId) {
        super("Growth track already exists with ID: " + growthTrackId);
    }

    public DuplicateGrowthTrackException(String field, String value) {
        super("Growth track already exists with " + field + ": " + value);
    }

    public DuplicateGrowthTrackException(String field, Object value) {
        super("Growth track already exists with " + field + ": " + value);
    }
}