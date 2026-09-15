package com.abdel.employee_management.service;

public interface PasswordResetService {
    void initiatePasswordReset(String email);
    void resetPassword(String token, String newPassword);
}