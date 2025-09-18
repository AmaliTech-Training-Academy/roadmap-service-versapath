package com.capstone.exception;

public class TalentRouteProcessingException extends RuntimeException {

    public TalentRouteProcessingException(String message) {
        super(message);
    }

    public TalentRouteProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public TalentRouteProcessingException(Throwable cause) {
        super("Failed to process talent route event", cause);
    }
}