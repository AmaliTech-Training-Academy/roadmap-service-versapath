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
        name = "capsule_atom_mapping",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_capsule_atom",
                        columnNames = {"skill_capsule_id", "skill_atom_id"}
                ),
                @UniqueConstraint(
                        name = "uk_capsule_sequence",
                        columnNames = {"skill_capsule_id", "sequence_order"}
                )
        },
        indexes = {
                @Index(name = "idx_capsule_mapping_capsule_id", columnList = "skill_capsule_id"),
                @Index(name = "idx_capsule_mapping_atom_id", columnList = "skill_atom_id"),
                @Index(name = "idx_capsule_mapping_sequence", columnList = "skill_capsule_id, sequence_order")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"skillCapsule", "skillAtom"})
public class CapsuleAtomMapping {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Many-to-One relationship with SkillCapsuleSnapshot
    @NotNull(message = "Skill capsule is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "skill_capsule_id", // column in mapping table
            referencedColumnName = "skill_capsule_id", // target column in skill_capsule_snapshot
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_capsule_mapping_capsule")
    )
    @JsonIgnore
    private SkillCapsuleSnapshot skillCapsule;

    // Many-to-One relationship with SkillAtomSnapshot
    @NotNull(message = "Skill atom is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "skill_atom_id", // column in mapping table
            referencedColumnName = "skill_atom_id", // target column in skill_atom_snapshot
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_capsule_mapping_atom")
    )
    private SkillAtomSnapshot skillAtom;

    // Sequence order for learning path
    @NotNull(message = "Sequence order is required")
    @Min(value = 1, message = "Sequence order must be at least 1")
    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    // Audit fields
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