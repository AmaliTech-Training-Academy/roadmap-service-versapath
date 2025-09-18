package com.capstone.exception;

import java.util.UUID;

public class DuplicateTalentRouteException extends RuntimeException {

    public DuplicateTalentRouteException(String message) {
        super(message);
    }

    public DuplicateTalentRouteException(UUID talentRouteId) {
        super("Talent route with ID " + talentRouteId + " already exists");
    }

    public DuplicateTalentRouteException(String field, String value) {
        super("Talent route with " + field + " '" + value + "' already exists");
    }
}