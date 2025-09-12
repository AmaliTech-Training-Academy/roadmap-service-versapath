package com.capstone.exception;

import java.util.UUID;

public class SkillAtomNotFoundException extends RuntimeException {
    public SkillAtomNotFoundException(String message) {
        super(message);
    }

    public SkillAtomNotFoundException(UUID skillAtomId) {
        super("Skill atom not found with ID: " + skillAtomId);
    }
    public SkillAtomNotFoundException(String field, String value) {
        super("Skill atom not found with " + field + ": " + value);
    }
}
