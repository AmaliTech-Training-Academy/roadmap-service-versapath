package com.capstone.exception;

public class DuplicateSkillAtomException extends RuntimeException{
    public DuplicateSkillAtomException(String message) {
        super(message);
    }

    public DuplicateSkillAtomException(String field, String value) {
        super("Skill atom already exists with " + field + ": " + value);
    }
}
