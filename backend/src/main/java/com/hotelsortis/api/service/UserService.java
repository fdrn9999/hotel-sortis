package com.hotelsortis.api.service;

import com.hotelsortis.api.dto.UserDto;
import com.hotelsortis.api.entity.Battle;
import com.hotelsortis.api.entity.Player;
import com.hotelsortis.api.entity.User;
import com.hotelsortis.api.repository.BattleRepository;
import com.hotelsortis.api.repository.PlayerRepository;
import com.hotelsortis.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final BattleRepository battleRepository;
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
     * 타 플레이어의 공개 프로필 조회
     */
    @Transactional(readOnly = true)
    public UserDto.PlayerPublicProfileDto getPlayerPublicProfile(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        // 전적 계산
        int wins = 0, losses = 0, draws = 0;
        List<Object[]> results = battleRepository.countPvPResultsByPlayerId(playerId);
        for (Object[] row : results) {
            Battle.Status status = (Battle.Status) row[0];
            Long count = (Long) row[1];
            switch (status) {
                case VICTORY -> wins = count.intValue();
                case DEFEAT -> losses = count.intValue();
                case DRAW -> draws = count.intValue();
            }
        }

        int totalMatches = wins + losses + draws;
        double winRate = totalMatches > 0 ? (double) wins / totalMatches : 0.0;

        return UserDto.PlayerPublicProfileDto.builder()
                .playerId(player.getId())
                .username(player.getUsername())
                .elo(player.getElo())
                .tier(calculateTier(player.getElo()))
                .currentFloor(player.getCurrentFloor())
                .highestFloorCleared(player.getHighestFloorCleared())
                .wins(wins)
                .losses(losses)
                .draws(draws)
                .winRate(winRate)
                .avatarId(player.getAvatarId())
                .build();
    }

    /**
     * 현재 사용자의 상세 통계 조회
     */
    @Transactional(readOnly = true)
    public UserDto.PlayerStatsDto getPlayerStats() {
        String email = getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Player player = playerRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        // 전적 계산
        int wins = 0, losses = 0, draws = 0;
        List<Object[]> results = battleRepository.countPvPResultsByPlayerId(player.getId());
        for (Object[] row : results) {
            Battle.Status status = (Battle.Status) row[0];
            Long count = (Long) row[1];
            switch (status) {
                case VICTORY -> wins = count.intValue();
                case DEFEAT -> losses = count.intValue();
                case DRAW -> draws = count.intValue();
            }
        }

        int totalMatches = wins + losses + draws;
        double winRate = totalMatches > 0 ? (double) wins / totalMatches : 0.0;

        return UserDto.PlayerStatsDto.builder()
                .wins(wins)
                .losses(losses)
                .draws(draws)
                .winRate(winRate)
                .totalMatches(totalMatches)
                .currentWinStreak(0) // TODO: implement win streak calculation
                .bestWinStreak(0)    // TODO: implement best win streak
                .build();
    }

    /**
     * 현재 사용자의 최근 매치 히스토리 조회
     */
    @Transactional(readOnly = true)
    public List<UserDto.MatchHistoryEntryDto> getMatchHistory(int limit) {
        String email = getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Player player = playerRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        List<Battle> battles = battleRepository.findRecentPvPBattles(
                player.getId(),
                PageRequest.of(0, limit)
        );

        List<UserDto.MatchHistoryEntryDto> history = new ArrayList<>();
        for (Battle battle : battles) {
            // 상대 플레이어 ID 결정
            Long opponentId = battle.getPlayerId().equals(player.getId())
                    ? battle.getEnemyId()
                    : battle.getPlayerId();

            // 상대 정보 조회
            String opponentName = "Unknown";
            Integer opponentElo = 0;
            if (opponentId != null) {
                Player opponent = playerRepository.findById(opponentId).orElse(null);
                if (opponent != null) {
                    opponentName = opponent.getUsername();
                    opponentElo = opponent.getElo();
                }
            }

            // 플레이어 관점의 결과 결정
            String result;
            if (battle.getPlayerId().equals(player.getId())) {
                result = battle.getStatus().name();
            } else {
                // 상대의 기록이므로 결과 반전
                result = switch (battle.getStatus()) {
                    case VICTORY -> "DEFEAT";
                    case DEFEAT -> "VICTORY";
                    default -> battle.getStatus().name();
                };
            }

            history.add(UserDto.MatchHistoryEntryDto.builder()
                    .battleId(battle.getId())
                    .opponentId(opponentId)
                    .opponentName(opponentName)
                    .opponentElo(opponentElo)
                    .result(result)
                    .eloChange(0) // TODO: store elo change in battle entity
                    .battleType(battle.getBattleType().name())
                    .createdAt(battle.getEndedAt())
                    .build());
        }

        return history;
    }

    /**
     * ELO 기반 티어 계산
     */
    private String calculateTier(int elo) {
        if (elo >= 2200) return "MASTER";
        if (elo >= 1900) return "DIAMOND";
        if (elo >= 1600) return "PLATINUM";
        if (elo >= 1300) return "GOLD";
        if (elo >= 1000) return "SILVER";
        return "BRONZE";
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
