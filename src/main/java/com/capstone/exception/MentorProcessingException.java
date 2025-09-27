package com.capstone.exception;

public class MentorProcessingException extends RuntimeException {
    public MentorProcessingException(String message) {
        super(message);
    }

    public MentorProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
