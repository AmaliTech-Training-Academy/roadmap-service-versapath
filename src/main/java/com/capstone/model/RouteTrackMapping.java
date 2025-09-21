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
        name = "route_track_mapping",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_route_track",
                        columnNames = {"talent_route_id", "growth_track_id"}
                ),
                @UniqueConstraint(

                        name = "uk_route_sequence",
                        columnNames = {"talent_route_id", "sequence_order"}
                )
        },
        indexes = {
                @Index(name = "idx_route_id", columnList = "talent_route_id"),
                @Index(name = "idx_track_id_route", columnList = "growth_track_id"),
                @Index(name = "idx_route_sequence", columnList = "talent_route_id, sequence_order")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"talentRoute", "growthTrack"})
public class RouteTrackMapping {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "Talent route is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "talent_route_id",
            referencedColumnName = "talent_route_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_route_mapping_route")
    )
    @JsonIgnore
    private TalentRouteSnapshot talentRoute;

    @NotNull(message = "Growth track is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "growth_track_id",
            referencedColumnName = "growth_track_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_route_mapping_track")
    )
    private GrowthTrackSnapshot growthTrack;

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
