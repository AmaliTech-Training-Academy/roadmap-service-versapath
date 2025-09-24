package com.capstone.exception;

import java.util.UUID;

public class TalentRouteNotFoundException extends RuntimeException {
    public TalentRouteNotFoundException(String message) {
        super(message);
    }
    public TalentRouteNotFoundException(UUID routeId) {
        super("Talent route not found with ID: " + routeId);
    }
}
