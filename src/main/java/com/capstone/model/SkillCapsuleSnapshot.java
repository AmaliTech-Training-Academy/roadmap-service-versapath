package com.capstone.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "skill_capsule_snapshot")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "learnerCapsuleProgresses")
public class SkillCapsuleSnapshot {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "Capsule name is required")
    @Size(max = 255, message = "Capsule name must not exceed 255 characters")
    @Column(name = "capsule_name", nullable = false, length = 255)
    private String capsuleName;

    @Size(max = 100, message = "Category type must not exceed 100 characters")
    @Column(name = "category_type", length = 100)
    private String categoryType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Min(value = 1, message = "Estimated hours must be at least 1")
    @Column(name = "estimated_hours", nullable = false)
    private Integer estimatedHours;

    @Column(name = "difficulty_level", length = 20)
    private String difficultyLevel;

    @OneToMany(mappedBy = "skillCapsule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LearnerCapsuleProgress> learnerCapsuleProgresses = new ArrayList<>();

}
