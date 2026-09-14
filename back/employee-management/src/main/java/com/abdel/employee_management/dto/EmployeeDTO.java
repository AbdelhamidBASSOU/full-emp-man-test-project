package com.abdel.employee_management.dto;

import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
public class EmployeeDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String jobTitle;
    private String department;
    private LocalDate hireDate;
    private BigDecimal salary;
    private String photoUrl;
    private String cvUrl;
}