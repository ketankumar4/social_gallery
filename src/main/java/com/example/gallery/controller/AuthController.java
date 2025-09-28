package com.example.gallery.controller;

import com.example.gallery.dto.ApiResponse;
import com.example.gallery.model.RefreshToken;
import com.example.gallery.model.User;
import com.example.gallery.repository.RefreshTokenRepository;
import com.example.gallery.repository.UserRepository;
import com.example.gallery.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired private UserRepository userRepo;
    @Autowired private RefreshTokenRepository refreshTokenRepo;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private AuthenticationManager authManager;

    // ---------- LOGIN ----------
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestParam String username, @RequestParam String password) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        User user = userRepo.findByUsername(username).orElseThrow();

        String accessToken = jwtUtil.generateAccessToken(username);
        String refreshTokenStr = jwtUtil.generateRefreshToken(username);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenStr);
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(jwtUtil.getRefreshTokenExpiry()));
        refreshTokenRepo.save(refreshToken);

        return ResponseEntity.ok(new ApiResponse<>("Login successful",
                new TokensResponse(accessToken, refreshTokenStr)));
    }

    // ---------- REFRESH TOKEN ----------
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<?>> refreshToken(@RequestParam("refreshToken") String refreshTokenStr) {
        RefreshToken oldToken = refreshTokenRepo.findByTokenAndRevokedFalse(refreshTokenStr)
                .orElseThrow(() -> new RuntimeException("Invalid or revoked refresh token"));

        if (oldToken.getExpiryDate().isBefore(Instant.now())) {
            oldToken.setRevoked(true);
            refreshTokenRepo.save(oldToken);
            throw new RuntimeException("Refresh token expired");
        }

        String username = jwtUtil.extractUsername(oldToken.getToken());

        // Rotate refresh token
        String newRefreshTokenStr = jwtUtil.generateRefreshToken(username);
        oldToken.setRevoked(true);
        refreshTokenRepo.save(oldToken);

        RefreshToken newToken = new RefreshToken();
        newToken.setToken(newRefreshTokenStr);
        newToken.setUser(oldToken.getUser());
        newToken.setExpiryDate(Instant.now().plusMillis(jwtUtil.getRefreshTokenExpiry()));
        refreshTokenRepo.save(newToken);

        String newAccessToken = jwtUtil.generateAccessToken(username);

        return ResponseEntity.ok(new ApiResponse<>("Token refreshed",
                new TokensResponse(newAccessToken, newRefreshTokenStr)));
    }

    // ---------- LOGOUT ----------
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(@RequestParam String refreshToken) {
        refreshTokenRepo.findByTokenAndRevokedFalse(refreshToken)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepo.save(token);
                });
        return ResponseEntity.ok(new ApiResponse<>("Logged out successfully", null));
    }

    // ---------- HELPER DTO ----------
    public static class TokensResponse {
        private String accessToken;
        private String refreshToken;

        public TokensResponse(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
    }
}
