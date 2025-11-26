package com.example.backend.service;

import com.example.backend.entity.User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {

    private static final Duration DEFAULT_TTL = Duration.ofHours(12);

    private final Map<String, TokenInfo> tokenStore = new ConcurrentHashMap<>();

    public String generateToken(User user) {
        String token = UUID.randomUUID().toString().replace("-", "");
        TokenInfo info = new TokenInfo(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                Instant.now().plus(DEFAULT_TTL)
        );
        tokenStore.put(token, info);
        return token;
    }

    public Optional<TokenInfo> validate(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        TokenInfo info = tokenStore.get(token);
        if (info == null) {
            return Optional.empty();
        }
        if (info.expireAt().isBefore(Instant.now())) {
            tokenStore.remove(token);
            return Optional.empty();
        }
        return Optional.of(info);
    }

    public record TokenInfo(Long userId, String username, String role, Instant expireAt) {}
}

