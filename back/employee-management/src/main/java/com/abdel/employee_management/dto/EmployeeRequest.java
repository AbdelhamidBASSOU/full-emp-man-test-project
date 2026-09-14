package com.abdel.employee_management.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
public class EmployeeRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email format must be valid")
    private String email;

    private String phoneNumber;

    @NotBlank(message = "Job title is required")
    private String jobTitle;

    private String department;

    @NotNull(message = "Hire date is required")
    private LocalDate hireDate;

    private BigDecimal salary;
}