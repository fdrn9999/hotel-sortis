package com.hotelsortis.api.dto;

import com.hotelsortis.api.entity.Avatar;
import com.hotelsortis.api.entity.DiceSkin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 코스메틱 DTO (i18n 지원)
 * CLAUDE.md i18n 규칙 참조
 */
public class CosmeticDto {

    /**
     * 주사위 스킨 응답 (사용자 언어에 맞게 변환)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiceSkinResponse {
        private Long id;
        private String skinCode;
        private String name;          // 사용자 언어의 이름
        private String description;   // 사용자 언어의 설명
        private String rarity;
        private Integer price;
        private String material;
        private String baseColor;
        private String pipColor;
        private String textureUrl;
        private Float metalness;
        private Float roughness;
        private String emissiveColor;
        private Float emissiveIntensity;
        private Boolean isDefault;
        private Boolean isAvailable;
        private Boolean isOwned;      // 플레이어 소유 여부
        private Boolean isEquipped;   // 장착 여부
        private String previewUrl;

        /**
         * DiceSkin 엔티티를 사용자 언어에 맞게 변환
         */
        public static DiceSkinResponse fromEntity(DiceSkin skin, String lang, boolean isOwned, boolean isEquipped) {
            String name;
            String description;

            switch (lang.toLowerCase()) {
                case "ko":
                    name = skin.getNameKo();
                    description = skin.getDescriptionKo();
                    break;
                case "ja":
                    name = skin.getNameJa();
                    description = skin.getDescriptionJa();
                    break;
                case "zh":
                    name = skin.getNameZh();
                    description = skin.getDescriptionZh();
                    break;
                case "en":
                default:
                    name = skin.getNameEn();
                    description = skin.getDescriptionEn();
                    break;
            }

            return DiceSkinResponse.builder()
                    .id(skin.getId())
                    .skinCode(skin.getSkinCode())
                    .name(name)
                    .description(description)
                    .rarity(skin.getRarity().name())
                    .price(skin.getPrice())
                    .material(skin.getMaterial())
                    .baseColor(skin.getBaseColor())
                    .pipColor(skin.getPipColor())
                    .textureUrl(skin.getTextureUrl())
                    .metalness(skin.getMetalness())
                    .roughness(skin.getRoughness())
                    .emissiveColor(skin.getEmissiveColor())
                    .emissiveIntensity(skin.getEmissiveIntensity())
                    .isDefault(skin.getIsDefault())
                    .isAvailable(skin.getIsAvailable())
                    .isOwned(isOwned)
                    .isEquipped(isEquipped)
                    .previewUrl(skin.getPreviewUrl())
                    .build();
        }
    }

    /**
     * 아바타 응답 (사용자 언어에 맞게 변환)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvatarResponse {
        private Long id;
        private String avatarCode;
        private String name;          // 사용자 언어의 이름
        private String description;   // 사용자 언어의 설명
        private String rarity;
        private Integer price;
        private String avatarUrl;
        private Boolean isDefault;
        private Boolean isAvailable;
        private Boolean isOwned;      // 플레이어 소유 여부
        private Boolean isEquipped;   // 장착 여부
        private String previewUrl;

        /**
         * Avatar 엔티티를 사용자 언어에 맞게 변환
         */
        public static AvatarResponse fromEntity(Avatar avatar, String lang, boolean isOwned, boolean isEquipped) {
            String name;
            String description;

            switch (lang.toLowerCase()) {
                case "ko":
                    name = avatar.getNameKo();
                    description = avatar.getDescriptionKo();
                    break;
                case "ja":
                    name = avatar.getNameJa();
                    description = avatar.getDescriptionJa();
                    break;
                case "zh":
                    name = avatar.getNameZh();
                    description = avatar.getDescriptionZh();
                    break;
                case "en":
                default:
                    name = avatar.getNameEn();
                    description = avatar.getDescriptionEn();
                    break;
            }

            return AvatarResponse.builder()
                    .id(avatar.getId())
                    .avatarCode(avatar.getAvatarCode())
                    .name(name)
                    .description(description)
                    .rarity(avatar.getRarity().name())
                    .price(avatar.getPrice())
                    .avatarUrl(avatar.getAvatarUrl())
                    .isDefault(avatar.getIsDefault())
                    .isAvailable(avatar.getIsAvailable())
                    .isOwned(isOwned)
                    .isEquipped(isEquipped)
                    .previewUrl(avatar.getPreviewUrl())
                    .build();
        }
    }

    /**
     * 코스메틱 컬렉션 응답 (플레이어가 소유한 코스메틱)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollectionResponse {
        private List<DiceSkinResponse> diceSkins;
        private List<AvatarResponse> avatars;
        private Long equippedDiceSkinId;
        private Long equippedAvatarId;
        private String language;
    }

    /**
     * 코스메틱 장착 요청
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EquipCosmeticRequest {
        private Long playerId;
        private String cosmeticType;  // "DICE_SKIN" or "AVATAR"
        private Long cosmeticId;
    }

    /**
     * 코스메틱 장착 응답
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EquipCosmeticResponse {
        private String message;
        private String cosmeticType;
        private Long cosmeticId;
        private Long equippedDiceSkinId;
        private Long equippedAvatarId;
    }
}
