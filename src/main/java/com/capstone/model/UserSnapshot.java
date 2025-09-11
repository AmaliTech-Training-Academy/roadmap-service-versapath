package com.capstone.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "user_snapshot")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSnapshot {
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "First name is required")
    @Size(max = 255, message = "First name must not exceed 255 characters")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 255, message = "Last name must not exceed 255 characters")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
