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
@Table(name = "skill_atom_snapshot")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "learnerAtomProgresses")
public class SkillAtomSnapshot {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "Skill atom ID is required")
    @Column(nullable = false, unique = true, updatable = false, name = "skill_atom_id")
    private UUID skillAtomId;

    @NotBlank(message = "Skill atom name is required")
    @Size(max = 255, message = "Skill name must not exceed 255 characters")
    @Column(name = "skill_atom_name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "moodle_module_id")
    private Integer moodleModuleId;

    @Column(name = "moodle_page_id")
    private Integer moodlePageId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "skillAtom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LearnerAtomProgress> learnerAtomProgresses = new ArrayList<>();

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
