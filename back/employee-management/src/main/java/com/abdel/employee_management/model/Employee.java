package com.abdel.employee_management.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "employees")
@Data
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phoneNumber;

    @Column(nullable = false)
    private String jobTitle;

    private String department;

    @Column(nullable = false)
    private LocalDate hireDate;

    private BigDecimal salary;

    private String photoUrl; // MinIO object key/reference

    private String cvUrl;    // MinIO object key/reference
}