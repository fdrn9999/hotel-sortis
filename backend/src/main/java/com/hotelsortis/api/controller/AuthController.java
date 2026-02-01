package com.hotelsortis.api.controller;

import com.hotelsortis.api.dto.AuthDto;
import com.hotelsortis.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입
     * POST /api/v1/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody AuthDto.SignupRequest request) {
        try {
            AuthDto.AuthResponse response = authService.signup(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Signup failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    AuthDto.MessageResponse.builder()
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    /**
     * 로그인
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDto.LoginRequest request) {
        try {
            AuthDto.AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    AuthDto.MessageResponse.builder()
                            .message("Invalid email or password")
                            .build()
            );
        }
    }

    /**
     * 헬스체크
     * GET /api/v1/auth/health
     */
    @GetMapping("/health")
    public ResponseEntity<AuthDto.MessageResponse> health() {
        return ResponseEntity.ok(
                AuthDto.MessageResponse.builder()
                        .message("Auth service is running")
                        .build()
        );
    }
}
