package com.capstone.exception;

public class SkillCapsuleProcessingException extends RuntimeException {

    public SkillCapsuleProcessingException(String message) {
        super(message);
    }

    public SkillCapsuleProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public SkillCapsuleProcessingException(Throwable cause) {
        super("Failed to process skill capsule event", cause);
    }
}
