package com.capstone.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "skill_capsule_snapshot")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"learnerCapsuleProgresses", "capsuleAtomMappings"})
public class SkillCapsuleSnapshot {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "Skill capsule ID is required")
    @Column(nullable = false, unique = true, updatable = false, name = "skill_capsule_id")
    private UUID skillCapsuleId;

    @NotBlank(message = "Capsule name is required")
    @Size(max = 255, message = "Capsule name must not exceed 255 characters")
    @Column(name = "capsule_name", nullable = false)
    private String capsuleName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "difficulty_level", length = 20)
    private String difficultyLevel;

    @Column(name = "moodle_course_id")
    private Integer moodleCourseId;

    @Size(max = 50, message = "Proficiency level must not exceed 50 characters")
    @Column(name = "proficiency_level", length = 50)
    private String proficiencyLevel;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "skillCapsule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LearnerCapsuleProgress> learnerCapsuleProgresses = new ArrayList<>();

    @OneToMany(mappedBy = "skillCapsule", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("sequenceOrder ASC")
    @Builder.Default
    private List<CapsuleAtomMapping> capsuleAtomMappings = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
