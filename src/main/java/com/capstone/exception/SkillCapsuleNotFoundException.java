package com.capstone.exception;

import java.util.UUID;

public class SkillCapsuleNotFoundException extends RuntimeException {

    public SkillCapsuleNotFoundException(String message) {
        super(message);
    }

    public SkillCapsuleNotFoundException(UUID skillCapsuleId) {
        super("Skill capsule not found with ID: " + skillCapsuleId);
    }

    public SkillCapsuleNotFoundException(String field, String value) {
        super("Skill capsule not found with " + field + ": " + value);
    }

    public SkillCapsuleNotFoundException(String field, Object value) {
        super("Skill capsule not found with " + field + ": " + value);
    }
}
