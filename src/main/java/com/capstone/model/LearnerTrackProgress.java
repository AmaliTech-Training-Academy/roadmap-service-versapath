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
@Table(name = "learner_track_progress")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"learnerRoadmap", "growthTrack", "learnerCapsuleProgresses"})
public class LearnerTrackProgress {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "Learner roadmap is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_roadmap_id", nullable = false)
    private LearnerRoadmap learnerRoadmap;

    @NotNull(message = "Growth track is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "growth_track_id", nullable = false)
    private GrowthTrackSnapshot growthTrack;

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

    @OneToMany(mappedBy = "learnerTrackProgress", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LearnerCapsuleProgress> learnerCapsuleProgresses = new ArrayList<>();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }
}
