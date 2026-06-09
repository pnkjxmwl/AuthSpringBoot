package com.project.auth_server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpirationSeconds;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    // ─── Create ────────────────────────────────────────────────────────────────

    public String createRefreshToken(String email) {
        String token = UUID.randomUUID().toString();
        String key = REFRESH_TOKEN_PREFIX + token;
        redisTemplate.opsForValue()
                .set(key, email, refreshTokenExpirationSeconds, TimeUnit.SECONDS);
        log.debug("Refresh token created for: {}", email);
        return token;
    }

    // ─── Validate ──────────────────────────────────────────────────────────────

    public Optional<String> getEmailByRefreshToken(String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        String email = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(email);
    }

    // ─── Delete ────────────────────────────────────────────────────────────────

    public void deleteRefreshToken(String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        log.info("DEBUG — Attempting to delete key: '{}'", key); 
        Boolean deleted = redisTemplate.delete(key);
        log.info("DEBUG — Deleted: {}", deleted); 
    }
}