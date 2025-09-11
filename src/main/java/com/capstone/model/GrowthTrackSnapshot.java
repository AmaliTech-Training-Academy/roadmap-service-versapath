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
@Table(name = "growth_track_snapshot")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "learnerTrackProgresses")
public class GrowthTrackSnapshot {
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "Track name is required")
    @Size(max = 50, message = "Track name must not exceed 50 characters")
    @Column(name = "track_name", nullable = false, length = 50)
    private String trackName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Min(value = 1, message = "Estimated months must be at least 1")
    @Column(name = "estimated_months", nullable = false)
    private Integer estimatedMonths;

    @OneToMany(mappedBy = "growthTrack", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LearnerTrackProgress> learnerTrackProgresses = new ArrayList<>();

}
