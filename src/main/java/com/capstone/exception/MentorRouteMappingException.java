package com.capstone.exception;

public class MentorRouteMappingException extends RuntimeException {
    public MentorRouteMappingException(String message) {
        super(message);
    }

    public MentorRouteMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
