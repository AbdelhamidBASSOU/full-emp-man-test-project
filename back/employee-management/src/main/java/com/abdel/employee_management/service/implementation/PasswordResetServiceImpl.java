package com.abdel.employee_management.service.implementation;

import com.abdel.employee_management.model.PasswordResetToken;
import com.abdel.employee_management.model.User;
import com.abdel.employee_management.repository.PasswordResetTokenRepository;
import com.abdel.employee_management.repository.UserRepository;
import com.abdel.employee_management.service.EmailService;
import com.abdel.employee_management.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private static final int TOKEN_EXPIRY_MINUTES = 30;

    @Override
    public void initiatePasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        // Deliberately do nothing detectable if the email doesn't exist —
        // prevents attackers from using this endpoint to discover which emails are registered.
        if (userOpt.isEmpty()) {
            return;
        }

        User user = userOpt.get();
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(
                token,
                user,
                LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES)
        );
        tokenRepository.save(resetToken);

        String resetLink = frontendUrl + "/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        if (resetToken.isUsed()) {
            throw new RuntimeException("This reset link has already been used");
        }

        if (resetToken.isExpired()) {
            throw new RuntimeException("This reset link has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }
}