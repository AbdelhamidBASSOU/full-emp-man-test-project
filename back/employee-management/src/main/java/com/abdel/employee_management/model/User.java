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

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType; // SUPER_ADMIN or NORMAL_USER

    private boolean canCreate = false;
    private boolean canRead = false;
    private boolean canUpdate = false;
    private boolean canDelete = false;

    private boolean enabled = true;

    public enum UserType {
        SUPER_ADMIN, NORMAL_USER
    }
}