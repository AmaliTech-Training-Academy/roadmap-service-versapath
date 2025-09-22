package com.capstone.exception;

import com.capstone.dto.response.ApiResponseDto;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
@Hidden
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleUserNotFoundException(UserNotFoundException ex) {
        log.error("User not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDto.error(ex.getMessage(), "User not found"));
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleDuplicateUserException(DuplicateUserException ex) {
        log.error("Duplicate user error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseDto.error(ex.getMessage(), "Duplicate user"));
    }

    @ExceptionHandler(UserProcessingException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleUserProcessingException(UserProcessingException ex) {
        log.error("User processing error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error(ex.getMessage(), "User processing failed"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMessage());

        String message = "Data integrity violation";
        String rootCause = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();

        // Check for specific constraint violations
        if (rootCause != null) {
            if (rootCause.contains("user_id")) {
                message = "User with this ID already exists";
            } else if (rootCause.contains("email")) {
                message = "User with this email already exists";
            } else if (rootCause.contains("username")) {
                message = "User with this username already exists";
            }
        }

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseDto.error(message, "Data constraint violation"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error(errors, "Validation failed"));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("Constraint violation: {}", ex.getMessage());

        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.error(errors, "Validation constraint violation"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error("An unexpected error occurred", "Internal server error"));
    }

    //SkillAtom Exceptions

    @ExceptionHandler(SkillAtomNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleSkillAtomNotFoundException(SkillAtomNotFoundException ex) {
        log.error("Skill atom not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDto.error(ex.getMessage(), "Skill atom not found"));
    }

    @ExceptionHandler(DuplicateSkillAtomException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleDuplicateSkillAtomException(DuplicateSkillAtomException ex) {
        log.error("Duplicate skill atom error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseDto.error(ex.getMessage(), "Duplicate skill atom"));
    }

    @ExceptionHandler(SkillAtomProcessingException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleSkillAtomProcessingException(SkillAtomProcessingException ex) {
        log.error("Skill atom processing error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error(ex.getMessage(), "Skill atom processing failed"));
    }

    // SkillCapsule Exceptions

    @ExceptionHandler(SkillCapsuleNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleSkillCapsuleNotFoundException(SkillCapsuleNotFoundException ex) {
        log.error("Skill capsule not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDto.error(ex.getMessage(), "Skill capsule not found"));
    }

    @ExceptionHandler(DuplicateSkillCapsuleException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleDuplicateSkillCapsuleException(DuplicateSkillCapsuleException ex) {
        log.error("Duplicate skill capsule error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseDto.error(ex.getMessage(), "Duplicate skill capsule"));
    }

    @ExceptionHandler(SkillCapsuleProcessingException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleSkillCapsuleProcessingException(SkillCapsuleProcessingException ex) {
        log.error("Skill capsule processing error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error(ex.getMessage(), "Skill capsule processing failed"));
    }

    @ExceptionHandler(CapsuleAtomMappingException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleCapsuleAtomMappingException(CapsuleAtomMappingException ex) {
        log.error("Capsule-atom mapping error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponseDto.error(ex.getMessage(), "Capsule-atom mapping failed"));
    }

    // GrowthTrack Exception

    @ExceptionHandler(GrowthTrackNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleGrowthTrackNotFoundException(GrowthTrackNotFoundException ex) {
        log.error("Growth track not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDto.error(ex.getMessage(), "Growth track not found"));
    }

    @ExceptionHandler(DuplicateGrowthTrackException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleDuplicateGrowthTrackException(DuplicateGrowthTrackException ex) {
        log.error("Duplicate growth track error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseDto.error(ex.getMessage(), "Duplicate growth track"));
    }

    @ExceptionHandler(GrowthTrackProcessingException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleGrowthTrackProcessingException(GrowthTrackProcessingException ex) {
        log.error("Growth track processing error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error(ex.getMessage(), "Growth track processing failed"));
    }

    @ExceptionHandler(TrackCapsuleMappingException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleTrackCapsuleMappingException(TrackCapsuleMappingException ex) {
        log.error("Track-capsule mapping error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponseDto.error(ex.getMessage(), "Track-capsule mapping failed"));
    }

    // GrowthTrack Exception

    @ExceptionHandler(DuplicateTalentRouteException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleDuplicateGrowthTrackException(DuplicateTalentRouteException ex) {
        log.error("Duplicate talent route error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseDto.error(ex.getMessage(), "Duplicate talent route"));
    }

    @ExceptionHandler(TalentRouteProcessingException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleGrowthTrackProcessingException(TalentRouteProcessingException ex) {
        log.error("Talent route processing error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.error(ex.getMessage(), "Talent route processing failed"));
    }

    @ExceptionHandler(RouteTrackMappingException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleTrackCapsuleMappingException(RouteTrackMappingException ex) {
        log.error("Route-track mapping error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponseDto.error(ex.getMessage(), "Route-track mapping failed"));
    }

    @ExceptionHandler(RoadmapNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleRoadmapNotFoundException(RoadmapNotFoundException ex) {
        log.error("Roadmap not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDto.error(ex.getMessage(), "Roadmap not found"));
    }

    @ExceptionHandler(RoadmapExistException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleRoadmapExistException(RoadmapExistException ex) {
        log.error("Roadmap exists: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseDto.error(ex.getMessage(), "Roadmap exists"));
    }

    @ExceptionHandler(ProgressNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleProgressNotFoundException(ProgressNotFoundException ex) {
        log.error("Progress not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDto.error(ex.getMessage(), "Progress not found"));
    }

    @ExceptionHandler(ProgressExistException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleProgressExistException(ProgressExistException ex) {
        log.error("Progress exist: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseDto.error(ex.getMessage(), "Progress exist"));
    }

    @ExceptionHandler(LessonException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleLessonException(LessonException ex) {
        log.error("Lesson exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseDto.error(ex.getMessage(), "Lesson Exception"));
    }

}
