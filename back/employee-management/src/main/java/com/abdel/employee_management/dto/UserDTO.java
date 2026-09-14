package com.abdel.employee_management.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String userType;
    private boolean canCreate;
    private boolean canRead;
    private boolean canUpdate;
    private boolean canDelete;
    private boolean enabled;
}