package com.abdel.employee_management.controller;

import com.abdel.employee_management.dto.ForgotPasswordRequest;
import com.abdel.employee_management.dto.LoginRequest;
import com.abdel.employee_management.dto.LoginResponse;
import com.abdel.employee_management.dto.ResetPasswordRequest;
import com.abdel.employee_management.model.RefreshToken;
import com.abdel.employee_management.model.User;
import com.abdel.employee_management.repository.UserRepository;
import com.abdel.employee_management.security.JwtUtil;
import com.abdel.employee_management.security.RateLimiterService;
import com.abdel.employee_management.service.PasswordResetService;
import com.abdel.employee_management.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private com.abdel.employee_management.security.CustomUserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private RateLimiterService rateLimiterService;

    private static final String REFRESH_COOKIE_NAME = "refreshToken";
    private static final int REFRESH_COOKIE_MAX_AGE = 60 * 60 * 24 * 7; // 7 days, in seconds

    @GetMapping("/csrf")
    public ResponseEntity<?> getCsrfToken() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest,
                                   HttpServletResponse response) {

        String rateLimitKey = "login:" + httpRequest.getRemoteAddr();
        if (!rateLimiterService.isAllowed(rateLimitKey, 5, Duration.ofMinutes(15))) {
            return ResponseEntity.status(429).body("Too many login attempts. Please try again later.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String accessToken = jwtUtil.generateToken(userDetails);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        addRefreshCookie(response, refreshToken.getToken());

        return ResponseEntity.ok(new LoginResponse(accessToken, user.getEmail(), user.getUserType().name()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request,
                                            HttpServletRequest httpRequest) {

        String rateLimitKey = "forgot-password:" + httpRequest.getRemoteAddr();
        if (!rateLimiterService.isAllowed(rateLimitKey, 3, Duration.ofMinutes(15))) {
            return ResponseEntity.status(429).body("Too many requests. Please try again later.");
        }

        passwordResetService.initiatePasswordReset(request.getEmail());

        // Always return the same generic message, whether the email exists —
        // prevents user enumeration via this endpoint.
        return ResponseEntity.ok("If that email is registered, a reset link has been sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request,
                                           HttpServletRequest httpRequest) {

        String rateLimitKey = "reset-password:" + httpRequest.getRemoteAddr();
        if (!rateLimiterService.isAllowed(rateLimitKey, 5, Duration.ofMinutes(15))) {
            return ResponseEntity.status(429).body("Too many attempts. Please try again later.");
        }

        try {
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }

        return ResponseEntity.ok("Password has been reset successfully.");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        String tokenFromCookie = extractRefreshCookie(request);

        if (tokenFromCookie == null) {
            return ResponseEntity.status(401).body("No refresh token provided");
        }

        RefreshToken refreshToken;
        try {
            refreshToken = refreshTokenService.validateRefreshToken(tokenFromCookie);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }

        User user = refreshToken.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String newAccessToken = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new LoginResponse(newAccessToken, user.getEmail(), user.getUserType().name()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String tokenFromCookie = extractRefreshCookie(request);

        if (tokenFromCookie != null) {
            try {
                RefreshToken refreshToken = refreshTokenService.validateRefreshToken(tokenFromCookie);
                refreshTokenService.revokeByUserId(refreshToken.getUser().getId());
            } catch (RuntimeException ignored) {
                // token already invalid/expired — nothing to revoke
            }
        }

        clearRefreshCookie(response);
        return ResponseEntity.ok().build();
    }

    private void addRefreshCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(REFRESH_COOKIE_MAX_AGE);
        response.addCookie(cookie);
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String extractRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (REFRESH_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}