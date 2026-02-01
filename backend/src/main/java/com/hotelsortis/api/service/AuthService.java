package com.hotelsortis.api.service;

import com.hotelsortis.api.dto.AuthDto;
import com.hotelsortis.api.entity.Player;
import com.hotelsortis.api.entity.User;
import com.hotelsortis.api.repository.PlayerRepository;
import com.hotelsortis.api.repository.UserRepository;
import com.hotelsortis.api.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * 회원가입
     */
    @Transactional
    public AuthDto.AuthResponse signup(AuthDto.SignupRequest request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        // User 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.UserRole.USER)
                .isActive(true)
                .emailVerified(false)
                .build();
        user = userRepository.save(user);

        // Player 생성 (User와 1:1)
        Player.Language language = parseLanguage(request.getPreferredLanguage());
        Player player = Player.builder()
                .user(user)
                .username(request.getUsername())
                .elo(1000)
                .soulStones(0)
                .currentFloor(1)
                .highestFloorCleared(0)
                .preferredLanguage(language)
                .build();
        player = playerRepository.save(player);

        // JWT 토큰 생성
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());

        log.info("User signed up successfully: {}", user.getEmail());

        return AuthDto.AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .username(player.getUsername())
                .playerId(player.getId())
                .role(user.getRole().name())
                .build();
    }

    /**
     * 로그인
     */
    @Transactional
    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        // 인증
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 마지막 로그인 시간 업데이트
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Player 조회
        Player player = playerRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        // JWT 토큰 생성
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());

        log.info("User logged in successfully: {}", user.getEmail());

        return AuthDto.AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .username(player.getUsername())
                .playerId(player.getId())
                .role(user.getRole().name())
                .build();
    }

    /**
     * 언어 코드 파싱
     */
    private Player.Language parseLanguage(String lang) {
        if (lang == null) {
            return Player.Language.en;
        }
        try {
            return Player.Language.valueOf(lang.toLowerCase());
        } catch (IllegalArgumentException e) {
            return Player.Language.en;
        }
    }
}
