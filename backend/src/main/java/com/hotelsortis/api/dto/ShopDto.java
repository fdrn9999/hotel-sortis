package com.hotelsortis.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 상점 DTO
 * Pay-to-Win 금지 - 코스메틱만 판매
 */
public class ShopDto {

    /**
     * 구매 요청
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseRequest {
        private Long playerId;
        private String cosmeticType;  // "DICE_SKIN" or "AVATAR"
        private Long cosmeticId;
    }

    /**
     * 구매 응답
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseResponse {
        private Boolean success;
        private String message;
        private String cosmeticType;
        private Long cosmeticId;
        private Integer pricePaid;
        private Integer remainingSoulStones;
    }

    /**
     * 상점 아이템 (주사위 스킨 또는 아바타)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShopItemResponse {
        private String cosmeticType;  // "DICE_SKIN" or "AVATAR"
        private Long id;
        private String code;
        private String name;
        private String description;
        private String rarity;
        private Integer price;
        private String previewUrl;
        private Boolean isOwned;
        private Boolean isPurchasable;  // 구매 가능 여부 (미소유 + 영혼석 충분)
    }

    /**
     * 상점 전체 응답
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShopResponse {
        private List<ShopItemResponse> diceSkins;
        private List<ShopItemResponse> avatars;
        private Integer playerSoulStones;
        private String language;
    }

    /**
     * 플레이어 재화 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayerWalletResponse {
        private Long playerId;
        private Integer soulStones;
        private Integer elo;
    }
}
