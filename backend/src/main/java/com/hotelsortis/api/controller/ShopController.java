package com.hotelsortis.api.controller;

import com.hotelsortis.api.dto.ShopDto;
import com.hotelsortis.api.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 상점 REST API 컨트롤러
 * 코스메틱 구매 및 영혼석 관리
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/shop")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ShopController {

    private final ShopService shopService;

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
     * 상점 조회 (구매 가능한 코스메틱 목록)
     * GET /api/v1/shop?playerId=123
     */
    @GetMapping
    public ResponseEntity<ShopDto.ShopResponse> getShop(
            @RequestParam Long playerId,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage
    ) {
        log.info("GET /api/v1/shop - playerId: {}, language: {}", playerId, acceptLanguage);

        String language = extractLanguageCode(acceptLanguage);
        ShopDto.ShopResponse shop = shopService.getShop(playerId, language);

        log.info("Returning shop with {} dice skins and {} avatars (Player soul stones: {})",
                shop.getDiceSkins().size(), shop.getAvatars().size(), shop.getPlayerSoulStones());
        return ResponseEntity.ok(shop);
    }

    /**
     * 코스메틱 구매
     * POST /api/v1/shop/purchase
     */
    @PostMapping("/purchase")
    public ResponseEntity<ShopDto.PurchaseResponse> purchaseCosmetic(
            @RequestBody ShopDto.PurchaseRequest request,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage
    ) {
        log.info("POST /api/v1/shop/purchase - playerId: {}, type: {}, cosmeticId: {}",
                request.getPlayerId(), request.getCosmeticType(), request.getCosmeticId());

        ShopDto.PurchaseResponse response = shopService.purchaseCosmetic(
                request.getPlayerId(),
                request
        );

        if (response.getSuccess()) {
            log.info("Purchase successful - Remaining soul stones: {}", response.getRemainingSoulStones());
        } else {
            log.warn("Purchase failed: {}", response.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 플레이어 지갑 조회 (영혼석, ELO)
     * GET /api/v1/shop/wallet?playerId=123
     */
    @GetMapping("/wallet")
    public ResponseEntity<ShopDto.PlayerWalletResponse> getPlayerWallet(
            @RequestParam Long playerId
    ) {
        log.info("GET /api/v1/shop/wallet - playerId: {}", playerId);

        ShopDto.PlayerWalletResponse wallet = shopService.getPlayerWallet(playerId);

        log.info("Returning wallet - Soul stones: {}, ELO: {}", wallet.getSoulStones(), wallet.getElo());
        return ResponseEntity.ok(wallet);
    }
}
