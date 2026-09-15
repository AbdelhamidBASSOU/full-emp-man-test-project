package com.abdel.employee_management.service.implementation;

import com.abdel.employee_management.model.RefreshToken;
import com.abdel.employee_management.model.User;
import com.abdel.employee_management.repository.RefreshTokenRepository;
import com.abdel.employee_management.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private static final long REFRESH_TOKEN_DURATION_MS = 1000L * 60 * 60 * 24 * 7; // 7 days

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // remove any existing refresh token for this user first (single active session)
        refreshTokenRepository.deleteByUserId(user.getId());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(REFRESH_TOKEN_DURATION_MS));
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token has expired");
        }

        return refreshToken;
    }

    @Override
    @Transactional
    public void revokeByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}