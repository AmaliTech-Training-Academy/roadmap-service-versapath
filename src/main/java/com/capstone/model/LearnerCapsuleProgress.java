package com.capstone.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "learner_capsule_progress")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"learnerTrackProgress", "skillCapsule", "learnerAtomProgresses"})
public class LearnerCapsuleProgress {
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "Learner track progress is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_track_progress_id", nullable = false)
    private LearnerTrackProgress learnerTrackProgress;

    @NotNull(message = "Skill capsule is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_capsule_id", nullable = false)
    private SkillCapsuleSnapshot skillCapsule;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ProgressStatus status = ProgressStatus.NOT_STARTED;

    @Builder.Default
    @Min(value = 0, message = "Progress percentage cannot be negative")
    @Max(value = 100, message = "Progress percentage cannot exceed 100")
    @Column(name = "progress_percentage")
    private Integer progressPercentage = 0;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "learnerCapsuleProgress", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LearnerAtomProgress> learnerAtomProgresses = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
