package com.abdel.employee_management.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserCreateRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email format must be valid")
    private String email;

    private String password;

    @NotBlank(message = "User type is required")
    private String userType;
}