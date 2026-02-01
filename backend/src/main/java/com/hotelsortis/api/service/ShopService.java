package com.hotelsortis.api.service;

import com.hotelsortis.api.dto.ShopDto;
import com.hotelsortis.api.entity.Avatar;
import com.hotelsortis.api.entity.DiceSkin;
import com.hotelsortis.api.entity.Player;
import com.hotelsortis.api.entity.PlayerCosmetic;
import com.hotelsortis.api.repository.AvatarRepository;
import com.hotelsortis.api.repository.DiceSkinRepository;
import com.hotelsortis.api.repository.PlayerCosmeticRepository;
import com.hotelsortis.api.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 상점 서비스
 * CLAUDE.md 규칙: Pay-to-Win 금지, 코스메틱만 판매, 영혼석으로만 구매
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShopService {

    private final DiceSkinRepository diceSkinRepository;
    private final AvatarRepository avatarRepository;
    private final PlayerCosmeticRepository playerCosmeticRepository;
    private final PlayerRepository playerRepository;

    /**
     * 상점 전체 조회 (구매 가능 여부 포함)
     */
    @Transactional(readOnly = true)
    public ShopDto.ShopResponse getShop(Long playerId, String language) {
        log.info("Getting shop for player {} in language: {}", playerId, language);

        String validatedLang = validateLanguage(language);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        List<ShopDto.ShopItemResponse> diceSkinItems = new ArrayList<>();
        List<ShopDto.ShopItemResponse> avatarItems = new ArrayList<>();

        // 주사위 스킨 조회
        List<DiceSkin> allDiceSkins = diceSkinRepository.findByIsAvailableTrue();
        for (DiceSkin skin : allDiceSkins) {
            boolean isOwned = playerCosmeticRepository.existsByPlayerIdAndCosmeticTypeAndCosmeticId(
                    playerId, PlayerCosmetic.CosmeticType.DICE_SKIN, skin.getId());
            boolean isPurchasable = !isOwned && player.getSoulStones() >= skin.getPrice();

            String name = getLocalizedName(skin.getNameKo(), skin.getNameEn(), skin.getNameJa(), skin.getNameZh(), validatedLang);
            String description = getLocalizedDescription(skin.getDescriptionKo(), skin.getDescriptionEn(),
                    skin.getDescriptionJa(), skin.getDescriptionZh(), validatedLang);

            diceSkinItems.add(ShopDto.ShopItemResponse.builder()
                    .cosmeticType("DICE_SKIN")
                    .id(skin.getId())
                    .code(skin.getSkinCode())
                    .name(name)
                    .description(description)
                    .rarity(skin.getRarity().name())
                    .price(skin.getPrice())
                    .previewUrl(skin.getPreviewUrl())
                    .isOwned(isOwned)
                    .isPurchasable(isPurchasable)
                    .build());
        }

        // 아바타 조회
        List<Avatar> allAvatars = avatarRepository.findByIsAvailableTrue();
        for (Avatar avatar : allAvatars) {
            boolean isOwned = playerCosmeticRepository.existsByPlayerIdAndCosmeticTypeAndCosmeticId(
                    playerId, PlayerCosmetic.CosmeticType.AVATAR, avatar.getId());
            boolean isPurchasable = !isOwned && player.getSoulStones() >= avatar.getPrice();

            String name = getLocalizedName(avatar.getNameKo(), avatar.getNameEn(), avatar.getNameJa(), avatar.getNameZh(), validatedLang);
            String description = getLocalizedDescription(avatar.getDescriptionKo(), avatar.getDescriptionEn(),
                    avatar.getDescriptionJa(), avatar.getDescriptionZh(), validatedLang);

            avatarItems.add(ShopDto.ShopItemResponse.builder()
                    .cosmeticType("AVATAR")
                    .id(avatar.getId())
                    .code(avatar.getAvatarCode())
                    .name(name)
                    .description(description)
                    .rarity(avatar.getRarity().name())
                    .price(avatar.getPrice())
                    .previewUrl(avatar.getPreviewUrl())
                    .isOwned(isOwned)
                    .isPurchasable(isPurchasable)
                    .build());
        }

        return ShopDto.ShopResponse.builder()
                .diceSkins(diceSkinItems)
                .avatars(avatarItems)
                .playerSoulStones(player.getSoulStones())
                .language(validatedLang)
                .build();
    }

    /**
     * 코스메틱 구매
     */
    @Transactional
    public ShopDto.PurchaseResponse purchaseCosmetic(Long playerId, ShopDto.PurchaseRequest request) {
        log.info("Purchasing cosmetic for player {}: type={}, id={}", playerId, request.getCosmeticType(), request.getCosmeticId());

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        PlayerCosmetic.CosmeticType cosmeticType = PlayerCosmetic.CosmeticType.valueOf(request.getCosmeticType());

        // 이미 소유 여부 확인
        boolean alreadyOwned = playerCosmeticRepository.existsByPlayerIdAndCosmeticTypeAndCosmeticId(
                playerId, cosmeticType, request.getCosmeticId());

        if (alreadyOwned) {
            return ShopDto.PurchaseResponse.builder()
                    .success(false)
                    .message("Already owned")
                    .cosmeticType(request.getCosmeticType())
                    .cosmeticId(request.getCosmeticId())
                    .pricePaid(0)
                    .remainingSoulStones(player.getSoulStones())
                    .build();
        }

        // 가격 확인 및 영혼석 차감
        int price;
        if (cosmeticType == PlayerCosmetic.CosmeticType.DICE_SKIN) {
            DiceSkin skin = diceSkinRepository.findById(request.getCosmeticId())
                    .orElseThrow(() -> new IllegalArgumentException("Dice skin not found: " + request.getCosmeticId()));

            if (!skin.getIsAvailable()) {
                return ShopDto.PurchaseResponse.builder()
                        .success(false)
                        .message("This item is not available for purchase")
                        .cosmeticType(request.getCosmeticType())
                        .cosmeticId(request.getCosmeticId())
                        .pricePaid(0)
                        .remainingSoulStones(player.getSoulStones())
                        .build();
            }

            price = skin.getPrice();
        } else {
            Avatar avatar = avatarRepository.findById(request.getCosmeticId())
                    .orElseThrow(() -> new IllegalArgumentException("Avatar not found: " + request.getCosmeticId()));

            if (!avatar.getIsAvailable()) {
                return ShopDto.PurchaseResponse.builder()
                        .success(false)
                        .message("This item is not available for purchase")
                        .cosmeticType(request.getCosmeticType())
                        .cosmeticId(request.getCosmeticId())
                        .pricePaid(0)
                        .remainingSoulStones(player.getSoulStones())
                        .build();
            }

            price = avatar.getPrice();
        }

        // 영혼석 충분한지 확인
        if (player.getSoulStones() < price) {
            return ShopDto.PurchaseResponse.builder()
                    .success(false)
                    .message("Not enough soul stones")
                    .cosmeticType(request.getCosmeticType())
                    .cosmeticId(request.getCosmeticId())
                    .pricePaid(0)
                    .remainingSoulStones(player.getSoulStones())
                    .build();
        }

        // 영혼석 차감
        player.setSoulStones(player.getSoulStones() - price);
        playerRepository.save(player);

        // 코스메틱 소유권 추가
        PlayerCosmetic cosmetic = PlayerCosmetic.builder()
                .player(player)
                .cosmeticType(cosmeticType)
                .cosmeticId(request.getCosmeticId())
                .build();
        playerCosmeticRepository.save(cosmetic);

        log.info("Purchase successful: player={}, type={}, id={}, price={}",
                playerId, request.getCosmeticType(), request.getCosmeticId(), price);

        return ShopDto.PurchaseResponse.builder()
                .success(true)
                .message("Purchase successful")
                .cosmeticType(request.getCosmeticType())
                .cosmeticId(request.getCosmeticId())
                .pricePaid(price)
                .remainingSoulStones(player.getSoulStones())
                .build();
    }

    /**
     * 플레이어 재화 정보 조회
     */
    @Transactional(readOnly = true)
    public ShopDto.PlayerWalletResponse getPlayerWallet(Long playerId) {
        log.info("Getting player wallet for player {}", playerId);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        return ShopDto.PlayerWalletResponse.builder()
                .playerId(player.getId())
                .soulStones(player.getSoulStones())
                .elo(player.getElo())
                .build();
    }

    /**
     * 언어 코드 검증 (CLAUDE.md i18n 규칙)
     */
    private String validateLanguage(String language) {
        if (language == null || language.trim().isEmpty()) {
            log.warn("Language not specified, defaulting to 'en'");
            return "en";
        }

        String lang = language.toLowerCase().trim();

        if (!List.of("ko", "en", "ja", "zh").contains(lang)) {
            log.warn("Unsupported language: {}, defaulting to 'en'", language);
            return "en";
        }

        return lang;
    }

    /**
     * 다국어 이름 조회
     */
    private String getLocalizedName(String ko, String en, String ja, String zh, String lang) {
        switch (lang) {
            case "ko": return ko;
            case "ja": return ja;
            case "zh": return zh;
            default: return en;
        }
    }

    /**
     * 다국어 설명 조회
     */
    private String getLocalizedDescription(String ko, String en, String ja, String zh, String lang) {
        switch (lang) {
            case "ko": return ko;
            case "ja": return ja;
            case "zh": return zh;
            default: return en;
        }
    }
}
