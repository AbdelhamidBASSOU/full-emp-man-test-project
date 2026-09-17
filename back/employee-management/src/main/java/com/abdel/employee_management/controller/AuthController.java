package com.abdel.employee_management.controller;

import com.abdel.employee_management.dto.ForgotPasswordRequest;
import com.abdel.employee_management.dto.ResetPasswordRequest;
import com.abdel.employee_management.dto.UserInfoResponse;
import com.abdel.employee_management.model.User;
import com.abdel.employee_management.security.PermissionChecker;
import com.abdel.employee_management.service.EmailService;
import com.abdel.employee_management.service.KeycloakAdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private PermissionChecker permissionChecker;

    @Autowired
    private EmailService emailService;

    @Autowired
    private KeycloakAdminService keycloakAdminService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser(JwtAuthenticationToken principal) {
        User user = permissionChecker.getCurrentUser();

        boolean isSuperAdmin = user.getUserType() == User.UserType.SUPER_ADMIN;
        UserInfoResponse response = new UserInfoResponse(
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

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String keycloakId = keycloakAdminService.findUserIdByEmail(request.getEmail());
        if (keycloakId != null) {
            String resetLink = frontendUrl + "/login?email=" + request.getEmail() + "&reset=true";
            emailService.sendPasswordResetEmail(request.getEmail(), resetLink);
        }
        // Always return success to prevent email enumeration
        return ResponseEntity.ok(Map.of("message", "If an account exists with that email, password reset instructions have been sent via Mailtrap."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        keycloakAdminService.updateUser(null, null, request.getEmail(), null, request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password has been successfully reset. You can now log in with your new password."));
    }
}