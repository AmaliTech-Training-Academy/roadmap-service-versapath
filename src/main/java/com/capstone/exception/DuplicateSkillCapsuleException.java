package com.capstone.exception;

import java.util.UUID;

public class DuplicateSkillCapsuleException extends RuntimeException {

    public DuplicateSkillCapsuleException(String message) {
        super(message);
    }

    public DuplicateSkillCapsuleException(UUID skillCapsuleId) {
        super("Skill capsule already exists with ID: " + skillCapsuleId);
    }

    public DuplicateSkillCapsuleException(String field, String value) {
        super("Skill capsule already exists with " + field + ": " + value);
    }

    public DuplicateSkillCapsuleException(String field, Object value) {
        super("Skill capsule already exists with " + field + ": " + value);
    }
}
