package com.capstone.model;

import jakarta.persistence.*;
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
@Table(name = "talent_route_snapshot")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"learnerRoadmaps", "routeTrackMappings"})
public class TalentRouteSnapshot {
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "Talent route ID is required")
    @Column(nullable = false, unique = true, updatable = false, name = "talent_route_id")
    private UUID talentRouteId;

    @NotBlank(message = "Route name is required")
    @Size(max = 50, message = "Route name must not exceed 50 characters")
    @Column(name = "route_name", nullable = false, length = 50)
    private String routeName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image", columnDefinition = "TEXT")
    private String image;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "talentRoute", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LearnerRoadmap> learnerRoadmaps = new ArrayList<>();

    @OneToMany(mappedBy = "talentRoute", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("sequenceOrder ASC")
    @Builder.Default
    private List<RouteTrackMapping> routeTrackMappings = new ArrayList<>();

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
