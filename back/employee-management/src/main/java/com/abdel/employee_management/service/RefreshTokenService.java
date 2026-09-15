package com.abdel.employee_management.service;

import com.abdel.employee_management.model.RefreshToken;
import com.abdel.employee_management.model.User;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user);
    RefreshToken validateRefreshToken(String token);
    void revokeByUserId(Long userId);
}