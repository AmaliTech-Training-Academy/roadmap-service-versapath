package com.capstone.exception;

import java.util.UUID;

public class DuplicateMentorException extends RuntimeException {
    public DuplicateMentorException(String message) {
        super(message);
    }

    public DuplicateMentorException(UUID mentorId) {
        super("Mentor already exists with ID: " + mentorId);
    }
}
