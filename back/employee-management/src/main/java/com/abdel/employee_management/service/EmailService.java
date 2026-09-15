package com.abdel.employee_management.service;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String resetLink);
}