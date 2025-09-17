package com.capstone.exception;

import java.util.UUID;

public class RouteTrackMappingException extends RuntimeException {

    public RouteTrackMappingException(String message) {
        super(message);
    }

    public RouteTrackMappingException(String message, Throwable cause) {
        super(message, cause);
    }

    public RouteTrackMappingException(UUID routeId, UUID trackId, String reason) {
        super("Failed to map track " + trackId + " to route " + routeId + ": " + reason);
    }

    public RouteTrackMappingException(UUID routeId, Integer sequenceOrder, String reason) {
        super("Failed to process sequence " + sequenceOrder + " for route " + routeId + ": " + reason);
    }
}