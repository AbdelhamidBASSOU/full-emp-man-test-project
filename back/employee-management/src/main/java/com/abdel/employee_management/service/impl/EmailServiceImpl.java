package com.abdel.employee_management.service.impl;

import com.abdel.employee_management.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Reset your password");
        message.setText(
                "You requested a password reset.\n\n" +
                        "Click the link below to reset your password:\n" +
                        resetLink + "\n\n" +
                        "This link expires in 30 minutes.\n" +
                        "If you didn't request this, you can safely ignore this email."
        );
        try {
            mailSender.send(message);
            log.info("Password reset email sent successfully via Mailtrap to {}", toEmail);
        } catch (Exception e) {
            log.warn("Failed to dispatch email via Mailtrap SMTP to {}: {}", toEmail, e.getMessage());
        }
    }
}