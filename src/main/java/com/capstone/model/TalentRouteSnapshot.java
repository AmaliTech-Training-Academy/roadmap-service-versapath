package com.capstone.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "talent_route_snapshot")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "learnerRoadmaps")
public class TalentRouteSnapshot {
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "Route name is required")
    @Size(max = 50, message = "Route name must not exceed 50 characters")
    @Column(name = "route_name", nullable = false, length = 50)
    private String routeName;

    @Size(max = 255, message = "Job role must not exceed 255 characters")
    @Column(name = "job_role", length = 255)
    private String jobRole;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "talentRoute", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LearnerRoadmap> learnerRoadmaps = new ArrayList<>();

}
