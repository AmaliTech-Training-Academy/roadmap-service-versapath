package com.capstone.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "learner_atom_progress")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"learnerCapsuleProgress", "skillAtom"})
public class LearnerAtomProgress {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "Learner capsule progress is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_capsule_progress_id", nullable = false)
    private LearnerCapsuleProgress learnerCapsuleProgress;

    @NotNull(message = "Skill atom is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_atom_id", nullable = false)
    private SkillAtomSnapshot skillAtom;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ProgressStatus status = ProgressStatus.NOT_STARTED;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
