package com.capstone.exception;

import java.util.UUID;

public class MentorNotFoundException extends RuntimeException {
    public MentorNotFoundException(String message) {
        super(message);
    }

    public MentorNotFoundException(UUID mentorId) {
        super("Mentor not found with ID: " + mentorId);
    }
}
