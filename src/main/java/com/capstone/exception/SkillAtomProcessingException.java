package com.capstone.exception;

public class SkillAtomProcessingException extends RuntimeException {
    public SkillAtomProcessingException(String message) {
        super(message);
    }
    public SkillAtomProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public SkillAtomProcessingException(Throwable cause) {
        super("Failed to process skill atom event", cause);
    }
}
