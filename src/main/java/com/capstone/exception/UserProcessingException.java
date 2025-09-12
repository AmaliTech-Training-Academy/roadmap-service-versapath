package com.capstone.exception;

public class UserProcessingException extends RuntimeException {
    public UserProcessingException(String message) {
        super(message);
    }
    public UserProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserProcessingException(Throwable cause) {
        super("Failed to process user event", cause);
    }
}
