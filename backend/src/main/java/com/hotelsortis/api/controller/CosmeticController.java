package com.hotelsortis.api.controller;

import com.hotelsortis.api.dto.CosmeticDto;
import com.hotelsortis.api.service.CosmeticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 코스메틱 REST API 컨트롤러
 * 주사위 스킨, 아바타 조회 및 장착 관리
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/cosmetics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CosmeticController {

    private final CosmeticService cosmeticService;

    /**
     * 언어 코드 추출 (Accept-Language 헤더)
     */
    private String extractLanguageCode(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isEmpty()) {
            return "en"; // 기본값: 영어
        }

        String[] languages = acceptLanguage.split(",");
        String primaryLanguage = languages[0].trim();

        if (primaryLanguage.contains("-")) {
            primaryLanguage = primaryLanguage.split("-")[0];
        }

        // ko, en, ja, zh만 지원
        if (primaryLanguage.matches("ko|en|ja|zh")) {
            return primaryLanguage;
        }

        return "en"; // 지원하지 않는 언어는 영어로
    }

    /**
     * 모든 주사위 스킨 조회 (소유 여부 포함)
     * GET /api/v1/cosmetics/dice-skins?playerId=123
     */
    @GetMapping("/dice-skins")
    public ResponseEntity<List<CosmeticDto.DiceSkinResponse>> getAllDiceSkins(
            @RequestParam Long playerId,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage
    ) {
        log.info("GET /api/v1/cosmetics/dice-skins - playerId: {}, language: {}", playerId, acceptLanguage);

        String language = extractLanguageCode(acceptLanguage);
        List<CosmeticDto.DiceSkinResponse> skins = cosmeticService.getAllDiceSkins(playerId, language);

        log.info("Returning {} dice skins", skins.size());
        return ResponseEntity.ok(skins);
    }

    /**
     * 모든 아바타 조회 (소유 여부 포함)
     * GET /api/v1/cosmetics/avatars?playerId=123
     */
    @GetMapping("/avatars")
    public ResponseEntity<List<CosmeticDto.AvatarResponse>> getAllAvatars(
            @RequestParam Long playerId,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage
    ) {
        log.info("GET /api/v1/cosmetics/avatars - playerId: {}, language: {}", playerId, acceptLanguage);

        String language = extractLanguageCode(acceptLanguage);
        List<CosmeticDto.AvatarResponse> avatars = cosmeticService.getAllAvatars(playerId, language);

        log.info("Returning {} avatars", avatars.size());
        return ResponseEntity.ok(avatars);
    }

    /**
     * 플레이어 컬렉션 조회 (보유 중인 코스메틱만)
     * GET /api/v1/cosmetics/collection?playerId=123
     */
    @GetMapping("/collection")
    public ResponseEntity<CosmeticDto.CollectionResponse> getPlayerCollection(
            @RequestParam Long playerId,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage
    ) {
        log.info("GET /api/v1/cosmetics/collection - playerId: {}, language: {}", playerId, acceptLanguage);

        String language = extractLanguageCode(acceptLanguage);
        CosmeticDto.CollectionResponse collection = cosmeticService.getPlayerCollection(playerId, language);

        log.info("Returning collection with {} dice skins and {} avatars",
                collection.getDiceSkins().size(), collection.getAvatars().size());
        return ResponseEntity.ok(collection);
    }

    /**
     * 코스메틱 장착
     * POST /api/v1/cosmetics/equip
     */
    @PostMapping("/equip")
    public ResponseEntity<CosmeticDto.EquipCosmeticResponse> equipCosmetic(
            @RequestBody CosmeticDto.EquipCosmeticRequest request
    ) {
        log.info("POST /api/v1/cosmetics/equip - playerId: {}, type: {}, cosmeticId: {}",
                request.getPlayerId(), request.getCosmeticType(), request.getCosmeticId());

        CosmeticDto.EquipCosmeticResponse response = cosmeticService.equipCosmetic(
                request.getPlayerId(),
                request
        );

        log.info("Cosmetic equipped successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * 코스메틱 장착 해제
     * DELETE /api/v1/cosmetics/equip?playerId=123&cosmeticType=DICE_SKIN
     */
    @DeleteMapping("/equip")
    public ResponseEntity<CosmeticDto.EquipCosmeticResponse> unequipCosmetic(
            @RequestParam Long playerId,
            @RequestParam String cosmeticType
    ) {
        log.info("DELETE /api/v1/cosmetics/equip - playerId: {}, type: {}", playerId, cosmeticType);

        CosmeticDto.EquipCosmeticResponse response = cosmeticService.unequipCosmetic(
                playerId,
                cosmeticType
        );

        log.info("Cosmetic unequipped successfully");
        return ResponseEntity.ok(response);
    }
}
