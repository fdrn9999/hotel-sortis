package com.hotelsortis.api.service;

import com.hotelsortis.api.dto.UserDto;
import com.hotelsortis.api.entity.Player;
import com.hotelsortis.api.entity.User;
import com.hotelsortis.api.repository.PlayerRepository;
import com.hotelsortis.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 현재 인증된 사용자 정보 조회
     */
    @Transactional(readOnly = true)
    public UserDto.UserProfileDto getProfile() {
        String email = getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Player player = playerRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        return UserDto.UserProfileDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .username(player.getUsername())
                .playerId(player.getId())
                .elo(player.getElo())
                .soulStones(player.getSoulStones())
                .currentFloor(player.getCurrentFloor())
                .highestFloorCleared(player.getHighestFloorCleared())
                .preferredLanguage(player.getPreferredLanguage().name())
                .role(user.getRole().name())
                .emailVerified(user.getEmailVerified())
                .build();
    }

    /**
     * 프로필 업데이트
     */
    @Transactional
    public UserDto.UserProfileDto updateProfile(UserDto.UpdateProfileDto request) {
        String email = getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Player player = playerRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        // 사용자명 변경
        if (request.getUsername() != null && !request.getUsername().equals(player.getUsername())) {
            if (playerRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Username already exists");
            }
            player.setUsername(request.getUsername());
        }

        // 선호 언어 변경
        if (request.getPreferredLanguage() != null) {
            try {
                Player.Language language = Player.Language.valueOf(request.getPreferredLanguage().toLowerCase());
                player.setPreferredLanguage(language);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid language: {}", request.getPreferredLanguage());
            }
        }

        playerRepository.save(player);
        log.info("Profile updated for user: {}", email);

        return getProfile();
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void changePassword(UserDto.ChangePasswordDto request) {
        String email = getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // 새 비밀번호 설정
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed for user: {}", email);
    }

    /**
     * 현재 인증된 사용자의 이메일 가져오기
     */
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }
        return authentication.getName();
    }
}
