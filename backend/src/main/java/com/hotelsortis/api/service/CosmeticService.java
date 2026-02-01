package com.hotelsortis.api.service;

import com.hotelsortis.api.dto.CosmeticDto;
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * 코스메틱 서비스 (i18n 지원)
 * CLAUDE.md 규칙: Pay-to-Win 금지, 코스메틱만 허용
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CosmeticService {

    private final DiceSkinRepository diceSkinRepository;
    private final AvatarRepository avatarRepository;
    private final PlayerCosmeticRepository playerCosmeticRepository;
    private final PlayerRepository playerRepository;

    /**
     * 모든 주사위 스킨 조회 (플레이어별 소유 여부 포함)
     */
    @Transactional(readOnly = true)
    public List<CosmeticDto.DiceSkinResponse> getAllDiceSkins(Long playerId, String language) {
        log.info("Getting all dice skins for player {} in language: {}", playerId, language);

        String validatedLang = validateLanguage(language);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        List<DiceSkin> allSkins = diceSkinRepository.findAll();
        List<DiceSkin> ownedSkins = diceSkinRepository.findOwnedByPlayer(playerId);
        List<Long> ownedSkinIds = ownedSkins.stream().map(DiceSkin::getId).collect(Collectors.toList());

        return allSkins.stream()
                .map(skin -> {
                    boolean isOwned = ownedSkinIds.contains(skin.getId());
                    boolean isEquipped = skin.getId().equals(player.getEquippedDiceSkinId());
                    return CosmeticDto.DiceSkinResponse.fromEntity(skin, validatedLang, isOwned, isEquipped);
                })
                .collect(Collectors.toList());
    }

    /**
     * 모든 아바타 조회 (플레이어별 소유 여부 포함)
     */
    @Transactional(readOnly = true)
    public List<CosmeticDto.AvatarResponse> getAllAvatars(Long playerId, String language) {
        log.info("Getting all avatars for player {} in language: {}", playerId, language);

        String validatedLang = validateLanguage(language);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        List<Avatar> allAvatars = avatarRepository.findAll();
        List<Avatar> ownedAvatars = avatarRepository.findOwnedByPlayer(playerId);
        List<Long> ownedAvatarIds = ownedAvatars.stream().map(Avatar::getId).collect(Collectors.toList());

        return allAvatars.stream()
                .map(avatar -> {
                    boolean isOwned = ownedAvatarIds.contains(avatar.getId());
                    boolean isEquipped = avatar.getId().equals(player.getEquippedAvatarId());
                    return CosmeticDto.AvatarResponse.fromEntity(avatar, validatedLang, isOwned, isEquipped);
                })
                .collect(Collectors.toList());
    }

    /**
     * 플레이어 코스메틱 컬렉션 조회 (소유한 것만)
     */
    @Transactional(readOnly = true)
    public CosmeticDto.CollectionResponse getPlayerCollection(Long playerId, String language) {
        log.info("Getting cosmetic collection for player {} in language: {}", playerId, language);

        String validatedLang = validateLanguage(language);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        List<DiceSkin> ownedSkins = diceSkinRepository.findOwnedByPlayer(playerId);
        List<Avatar> ownedAvatars = avatarRepository.findOwnedByPlayer(playerId);

        List<CosmeticDto.DiceSkinResponse> skinResponses = ownedSkins.stream()
                .map(skin -> {
                    boolean isEquipped = skin.getId().equals(player.getEquippedDiceSkinId());
                    return CosmeticDto.DiceSkinResponse.fromEntity(skin, validatedLang, true, isEquipped);
                })
                .collect(Collectors.toList());

        List<CosmeticDto.AvatarResponse> avatarResponses = ownedAvatars.stream()
                .map(avatar -> {
                    boolean isEquipped = avatar.getId().equals(player.getEquippedAvatarId());
                    return CosmeticDto.AvatarResponse.fromEntity(avatar, validatedLang, true, isEquipped);
                })
                .collect(Collectors.toList());

        return CosmeticDto.CollectionResponse.builder()
                .diceSkins(skinResponses)
                .avatars(avatarResponses)
                .equippedDiceSkinId(player.getEquippedDiceSkinId())
                .equippedAvatarId(player.getEquippedAvatarId())
                .language(validatedLang)
                .build();
    }

    /**
     * 코스메틱 장착
     */
    @Transactional
    public CosmeticDto.EquipCosmeticResponse equipCosmetic(Long playerId, CosmeticDto.EquipCosmeticRequest request) {
        log.info("Equipping cosmetic for player {}: type={}, id={}", playerId, request.getCosmeticType(), request.getCosmeticId());

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        PlayerCosmetic.CosmeticType cosmeticType = PlayerCosmetic.CosmeticType.valueOf(request.getCosmeticType());

        // 소유 여부 확인
        boolean isOwned = playerCosmeticRepository.existsByPlayerIdAndCosmeticTypeAndCosmeticId(
                playerId, cosmeticType, request.getCosmeticId());

        if (!isOwned) {
            throw new IllegalArgumentException("Player does not own this cosmetic");
        }

        // 장착
        if (cosmeticType == PlayerCosmetic.CosmeticType.DICE_SKIN) {
            player.setEquippedDiceSkinId(request.getCosmeticId());
        } else {
            player.setEquippedAvatarId(request.getCosmeticId());
        }

        playerRepository.save(player);

        log.info("Cosmetic equipped successfully for player {}", playerId);

        return CosmeticDto.EquipCosmeticResponse.builder()
                .message("Cosmetic equipped successfully")
                .cosmeticType(request.getCosmeticType())
                .cosmeticId(request.getCosmeticId())
                .equippedDiceSkinId(player.getEquippedDiceSkinId())
                .equippedAvatarId(player.getEquippedAvatarId())
                .build();
    }

    /**
     * 코스메틱 장착 해제
     */
    @Transactional
    public CosmeticDto.EquipCosmeticResponse unequipCosmetic(Long playerId, String cosmeticType) {
        log.info("Unequipping cosmetic for player {}: type={}", playerId, cosmeticType);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        PlayerCosmetic.CosmeticType type = PlayerCosmetic.CosmeticType.valueOf(cosmeticType);

        // 해제
        if (type == PlayerCosmetic.CosmeticType.DICE_SKIN) {
            player.setEquippedDiceSkinId(null);
        } else {
            player.setEquippedAvatarId(null);
        }

        playerRepository.save(player);

        log.info("Cosmetic unequipped successfully for player {}", playerId);

        return CosmeticDto.EquipCosmeticResponse.builder()
                .message("Cosmetic unequipped successfully")
                .cosmeticType(cosmeticType)
                .cosmeticId(null)
                .equippedDiceSkinId(player.getEquippedDiceSkinId())
                .equippedAvatarId(player.getEquippedAvatarId())
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
}
