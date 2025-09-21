package com.capstone.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "track_capsule_mapping",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_track_capsule",
                        columnNames = {"growth_track_id", "skill_capsule_id"}
                ),
                @UniqueConstraint(
                        name = "uk_track_sequence",
                        columnNames = {"growth_track_id", "sequence_order"}
                )
        },
        indexes = {
                @Index(name = "idx_track_mapping_track_id", columnList = "growth_track_id"),
                @Index(name = "idx_track_mapping_capsule_id", columnList = "skill_capsule_id"),
                @Index(name = "idx_track_mapping_sequence", columnList = "growth_track_id, sequence_order")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"growthTrack", "skillCapsule"})
public class TrackCapsuleMapping {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "Growth track is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "growth_track_id",
            referencedColumnName = "growth_track_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_track_mapping_track")
    )
    @JsonIgnore
    private GrowthTrackSnapshot growthTrack;

    @NotNull(message = "Skill capsule is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "skill_capsule_id",
            referencedColumnName = "skill_capsule_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_track_mapping_capsule")
    )
    private SkillCapsuleSnapshot skillCapsule;

    @NotNull(message = "Sequence order is required")
    @Min(value = 1, message = "Sequence order must be at least 1")
    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

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
