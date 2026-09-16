package com.abdel.employee_management.controller;

import com.abdel.employee_management.dto.LoginResponse;
import com.abdel.employee_management.model.User;
import com.abdel.employee_management.security.PermissionChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private PermissionChecker permissionChecker;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(JwtAuthenticationToken principal) {
        User user = permissionChecker.getCurrentUser();

        boolean isSuperAdmin = user.getUserType() == User.UserType.SUPER_ADMIN;
        LoginResponse response = new LoginResponse(
                null,
                user.getEmail(),
                user.getUserType().name(),
                isSuperAdmin || user.isCanCreate(),
                isSuperAdmin || user.isCanRead(),
                isSuperAdmin || user.isCanUpdate(),
                isSuperAdmin || user.isCanDelete()
        );
        return ResponseEntity.ok(response);
    }
}