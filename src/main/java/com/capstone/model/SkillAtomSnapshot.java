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

    @NotBlank(message = "Skill name is required")
    @Size(max = 255, message = "Skill name must not exceed 255 characters")
    @Column(name = "skill_name", nullable = false, length = 255)
    private String skillName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "skillAtom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LearnerAtomProgress> learnerAtomProgresses = new ArrayList<>();
}
