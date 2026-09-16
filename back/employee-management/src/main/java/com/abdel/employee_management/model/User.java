package com.abdel.employee_management.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String keycloakId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType;

    private boolean canCreate = false;
    private boolean canRead = false;
    private boolean canUpdate = false;
    private boolean canDelete = false;

    private boolean enabled = true;

    public enum UserType {
        SUPER_ADMIN, NORMAL_USER
    }
}