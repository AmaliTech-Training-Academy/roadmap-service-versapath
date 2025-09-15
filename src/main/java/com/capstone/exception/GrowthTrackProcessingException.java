package com.capstone.exception;

public class GrowthTrackProcessingException extends RuntimeException {

    public GrowthTrackProcessingException(String message) {
        super(message);
    }

    public GrowthTrackProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public GrowthTrackProcessingException(Throwable cause) {
        super("Failed to process growth track event", cause);
    }
}
