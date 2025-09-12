package com.capstone.exception;

public class DuplicateUserException extends RuntimeException {
    public DuplicateUserException(String message) {
        super(message);
    }

    public DuplicateUserException(String field, String value) {
        super("User already exists with " + field + ": " + value);
    }
}
