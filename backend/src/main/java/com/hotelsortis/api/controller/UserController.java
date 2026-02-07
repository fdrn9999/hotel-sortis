package com.hotelsortis.api.controller;

import com.hotelsortis.api.dto.AuthDto;
import com.hotelsortis.api.dto.UserDto;
import com.hotelsortis.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 내 프로필 조회
     * GET /api/v1/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<EntityModel<UserDto.UserProfileDto>> getMyProfile() {
        UserDto.UserProfileDto profile = userService.getProfile();

        EntityModel<UserDto.UserProfileDto> resource = EntityModel.of(profile);
        resource.add(linkTo(methodOn(UserController.class).getMyProfile()).withSelfRel());
        resource.add(linkTo(methodOn(UserController.class).updateProfile(null)).withRel("update"));

        return ResponseEntity.ok(resource);
    }

    /**
     * 프로필 업데이트
     * PUT /api/v1/users/me
     */
    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UserDto.UpdateProfileDto request) {
        try {
            UserDto.UserProfileDto profile = userService.updateProfile(request);

            EntityModel<UserDto.UserProfileDto> resource = EntityModel.of(profile);
            resource.add(linkTo(methodOn(UserController.class).getMyProfile()).withSelfRel());

            return ResponseEntity.ok(resource);
        } catch (IllegalArgumentException e) {
            log.error("Profile update failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    AuthDto.MessageResponse.builder()
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    /**
     * 비밀번호 변경
     * POST /api/v1/users/me/change-password
     */
    @PostMapping("/me/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody UserDto.ChangePasswordDto request) {
        try {
            userService.changePassword(request);
            return ResponseEntity.ok(
                    AuthDto.MessageResponse.builder()
                            .message("Password changed successfully")
                            .build()
            );
        } catch (IllegalArgumentException e) {
            log.error("Password change failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    AuthDto.MessageResponse.builder()
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    /**
     * 타 플레이어 프로필 조회
     * GET /api/v1/users/{playerId}/profile
     */
    @GetMapping("/{playerId}/profile")
    public ResponseEntity<?> getPlayerProfile(@PathVariable Long playerId) {
        try {
            UserDto.PlayerPublicProfileDto profile = userService.getPlayerPublicProfile(playerId);
            return ResponseEntity.ok(profile);
        } catch (IllegalArgumentException e) {
            log.error("Player profile not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 내 통계 조회
     * GET /api/v1/users/me/stats
     */
    @GetMapping("/me/stats")
    public ResponseEntity<UserDto.PlayerStatsDto> getMyStats() {
        UserDto.PlayerStatsDto stats = userService.getPlayerStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * 내 매치 히스토리 조회
     * GET /api/v1/users/me/match-history?limit=10
     */
    @GetMapping("/me/match-history")
    public ResponseEntity<List<UserDto.MatchHistoryEntryDto>> getMatchHistory(
            @RequestParam(defaultValue = "10") int limit) {
        List<UserDto.MatchHistoryEntryDto> history = userService.getMatchHistory(limit);
        return ResponseEntity.ok(history);
    }
}
