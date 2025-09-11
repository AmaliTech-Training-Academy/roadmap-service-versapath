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
@Table(name = "learner_roadmap")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"talentRoute", "learnerTrackProgresses"})
public class LearnerRoadmap {
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotNull(message = "Talent route is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "talent_route_id", nullable = false)
    private TalentRouteSnapshot talentRoute;

    @Enumerated(EnumType.STRING)
    @Column(name = "enrollment_status", nullable = false, length = 50)
    private EnrollmentStatus enrollmentStatus;

    @Column(name = "completion_date")
    private LocalDateTime completionDate;

    @Builder.Default
    @Min(value = 0, message = "Progress percentage cannot be negative")
    @Max(value = 100, message = "Progress percentage cannot exceed 100")
    @Column(name = "overall_progress_percentage")
    private Integer overallProgressPercentage = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "learnerRoadmap", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LearnerTrackProgress> learnerTrackProgresses = new ArrayList<>();

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
