package com.capstone.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "mentor_route_mapping",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_mentor_route",
                        columnNames = {"mentor_id", "talent_route_id"}
                )
        },
        indexes = {
                @Index(name = "idx_mentor_mapping_mentor_id", columnList = "mentor_id"),
                @Index(name = "idx_mentor_mapping_route_id", columnList = "talent_route_id")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"mentorSnapshot", "talentRoute"})
public class MentorRouteMapping {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "Mentor is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "mentor_id",
            referencedColumnName = "mentor_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_mentor_mapping_mentor")
    )
    @JsonIgnore
    private MentorSnapshot mentorSnapshot;

    @NotNull(message = "Talent route is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "talent_route_id",
            referencedColumnName = "talent_route_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_mentor_mapping_route")
    )
    private TalentRouteSnapshot talentRoute;

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
