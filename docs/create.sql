-- =====================================================
-- HOTEL SORTIS - Table Creation Script
-- 호텔 소르티스 - 테이블 생성 스크립트
-- =====================================================
-- 실행: mysql -u root -p hotel_sortis < create.sql
-- =====================================================

-- 데이터베이스 생성 (필요시 주석 해제)
-- CREATE DATABASE IF NOT EXISTS hotel_sortis CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE hotel_sortis;

-- =====================================================
-- 기존 테이블 삭제 (개발용, 운영시 주석 처리)
-- =====================================================
DROP TABLE IF EXISTS player_cosmetics;
DROP TABLE IF EXISTS dice_skins;
DROP TABLE IF EXISTS avatars;
DROP TABLE IF EXISTS battle_logs;
DROP TABLE IF EXISTS battles;
DROP TABLE IF EXISTS player_skills;
DROP TABLE IF EXISTS campaign_progress;
DROP TABLE IF EXISTS players;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS skills;
DROP TABLE IF EXISTS bosses;
DROP TABLE IF EXISTS floors;

-- =====================================================
-- 1. 사용자 테이블 (users)
-- =====================================================
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL COMMENT 'BCrypt 해시',
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='사용자 인증 정보';

-- =====================================================
-- 2. 플레이어 테이블 (players)
-- =====================================================
CREATE TABLE players (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE COMMENT '사용자 ID (1:1 관계)',
    username VARCHAR(20) NOT NULL UNIQUE COMMENT '게임 내 닉네임',
    elo INT NOT NULL DEFAULT 1000,
    soul_stones INT NOT NULL DEFAULT 0,
    current_floor INT NOT NULL DEFAULT 1,
    highest_floor_cleared INT NOT NULL DEFAULT 0,
    equipped_skill_ids JSON COMMENT '장착된 스킬 ID 배열 (최대 4개)',
    equipped_dice_skin_id BIGINT COMMENT '장착 중인 주사위 스킨 ID',
    equipped_avatar_id BIGINT COMMENT '장착 중인 아바타 ID',
    preferred_language ENUM('ko', 'en', 'ja', 'zh') NOT NULL DEFAULT 'en',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_username (username),
    INDEX idx_elo (elo),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='플레이어 게임 정보';

-- =====================================================
-- 3. 스킬 테이블 (skills)
-- =====================================================
CREATE TABLE skills (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    skill_code VARCHAR(50) NOT NULL UNIQUE COMMENT '스킬 고유 코드',
    name_en VARCHAR(100) NOT NULL,
    name_ko VARCHAR(100) NOT NULL,
    name_ja VARCHAR(100) NOT NULL,
    name_zh VARCHAR(100) NOT NULL,
    rarity ENUM('Common', 'Rare', 'Epic', 'Legendary') NOT NULL,
    description_en TEXT NOT NULL,
    description_ko TEXT NOT NULL,
    description_ja TEXT NOT NULL,
    description_zh TEXT NOT NULL,
    trigger_type ENUM('BATTLE_START', 'TURN_START', 'TURN_END', 'DICE_ROLL', 'BEFORE_DAMAGE', 'AFTER_DAMAGE', 'PASSIVE') NOT NULL,
    effect_json JSON NOT NULL COMMENT '스킬 효과 정의',
    icon_url VARCHAR(255),
    unlock_floor INT DEFAULT NULL COMMENT '해금되는 층 (NULL이면 기본 제공)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_rarity (rarity),
    INDEX idx_trigger (trigger_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='스킬 정보';

-- =====================================================
-- 4. 플레이어-스킬 연결 테이블 (player_skills)
-- =====================================================
CREATE TABLE player_skills (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    player_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    unlocked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE,
    UNIQUE KEY uk_player_skill (player_id, skill_id),
    INDEX idx_player_id (player_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='플레이어가 보유한 스킬';

-- =====================================================
-- 5. 전투 테이블 (battles)
-- =====================================================
CREATE TABLE battles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    player_id BIGINT NOT NULL,
    enemy_id BIGINT COMMENT 'PvP시 상대 플레이어 ID, PvE면 NULL',
    battle_type ENUM('PVE', 'PVP') NOT NULL,
    floor INT COMMENT 'PvE 층 번호',
    boss_id VARCHAR(50) COMMENT '보스 ID (보스전인 경우)',
    boss_phase INT DEFAULT 1 COMMENT '현재 보스 페이즈',
    status ENUM('ONGOING', 'VICTORY', 'DEFEAT', 'DRAW') NOT NULL DEFAULT 'ONGOING',
    player_hp INT NOT NULL DEFAULT 100,
    enemy_hp INT NOT NULL DEFAULT 100,
    turn_count INT NOT NULL DEFAULT 1,
    current_turn ENUM('PLAYER', 'ENEMY') NOT NULL DEFAULT 'PLAYER',
    player_equipped_skills JSON COMMENT '플레이어 장착 스킬',
    enemy_equipped_skills JSON COMMENT '적 장착 스킬',
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
    INDEX idx_player_id (player_id),
    INDEX idx_status (status),
    INDEX idx_battle_type (battle_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='전투 기록';

-- =====================================================
-- 6. 전투 로그 테이블 (battle_logs)
-- =====================================================
CREATE TABLE battle_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    battle_id BIGINT NOT NULL,
    turn_number INT NOT NULL,
    actor ENUM('PLAYER', 'ENEMY') NOT NULL,
    dice_result JSON NOT NULL COMMENT '주사위 결과 [d1, d2, d3]',
    hand_rank VARCHAR(20) NOT NULL COMMENT '족보 영문명',
    hand_rank_ko VARCHAR(20) NOT NULL COMMENT '족보 한글명',
    hand_rank_ja VARCHAR(20) NOT NULL COMMENT '족보 일본어명',
    hand_rank_zh VARCHAR(20) NOT NULL COMMENT '족보 중국어명',
    hand_power INT NOT NULL COMMENT '족보 공격력',
    damage_dealt INT NOT NULL COMMENT '실제 데미지',
    skills_activated JSON COMMENT '발동된 스킬 ID 배열',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (battle_id) REFERENCES battles(id) ON DELETE CASCADE,
    INDEX idx_battle_id (battle_id),
    INDEX idx_turn (battle_id, turn_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='전투 턴별 로그';

-- =====================================================
-- 7. 캠페인 진행도 테이블 (campaign_progress)
-- =====================================================
CREATE TABLE campaign_progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    player_id BIGINT NOT NULL UNIQUE,
    current_run_floor INT NOT NULL DEFAULT 1 COMMENT '현재 런 진행 층',
    current_run_hp INT NOT NULL DEFAULT 100 COMMENT '현재 런 HP',
    floor_1_cleared BOOLEAN DEFAULT FALSE,
    floor_2_cleared BOOLEAN DEFAULT FALSE,
    floor_3_cleared BOOLEAN DEFAULT FALSE,
    floor_4_cleared BOOLEAN DEFAULT FALSE,
    floor_5_cleared BOOLEAN DEFAULT FALSE,
    floor_6_cleared BOOLEAN DEFAULT FALSE,
    floor_7_cleared BOOLEAN DEFAULT FALSE,
    floor_8_cleared BOOLEAN DEFAULT FALSE,
    floor_9_cleared BOOLEAN DEFAULT FALSE,
    floor_10_cleared BOOLEAN DEFAULT FALSE,
    floor_11_cleared BOOLEAN DEFAULT FALSE,
    floor_12_cleared BOOLEAN DEFAULT FALSE,
    floor_13_cleared BOOLEAN DEFAULT FALSE,
    floor_14_cleared BOOLEAN DEFAULT FALSE,
    floor_15_cleared BOOLEAN DEFAULT FALSE,
    mammon_defeated BOOLEAN DEFAULT FALSE COMMENT '5층 보스 격파',
    eligor_defeated BOOLEAN DEFAULT FALSE COMMENT '10층 보스 격파',
    lucifuge_defeated BOOLEAN DEFAULT FALSE COMMENT '15층 보스 격파',
    total_runs INT NOT NULL DEFAULT 0,
    total_victories INT NOT NULL DEFAULT 0,
    total_defeats INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='캠페인 진행 상황';

-- =====================================================
-- 8. 층 정보 테이블 (floors)
-- =====================================================
CREATE TABLE floors (
    id INT PRIMARY KEY,
    floor_type ENUM('NORMAL', 'ELITE', 'BOSS') NOT NULL,
    battle_count INT NOT NULL COMMENT '전투 횟수',
    boss_id VARCHAR(50) COMMENT '보스 ID (보스층인 경우)',
    ai_level INT NOT NULL DEFAULT 0 COMMENT 'AI 레벨 (0-3)',
    enemy_skill_count INT NOT NULL DEFAULT 0 COMMENT '적 스킬 개수',
    skill_reward_rarity ENUM('Common', 'Rare', 'Epic', 'Legendary') COMMENT '보상 스킬 등급',
    description_ko VARCHAR(255),
    description_en VARCHAR(255),
    description_ja VARCHAR(255),
    description_zh VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='층별 설정';

-- =====================================================
-- 9. 보스 테이블 (bosses)
-- =====================================================
CREATE TABLE bosses (
    id VARCHAR(50) PRIMARY KEY,
    name_en VARCHAR(100) NOT NULL,
    name_ko VARCHAR(100) NOT NULL,
    name_ja VARCHAR(100) NOT NULL,
    name_zh VARCHAR(100) NOT NULL,
    floor INT NOT NULL COMMENT '등장 층',
    total_phases INT NOT NULL COMMENT '총 페이즈 수',
    phase_config JSON NOT NULL COMMENT '페이즈별 설정 (AI레벨, 스킬, 패턴 등)',
    quotes JSON COMMENT '대사 모음',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='보스 정보';

-- =====================================================
-- 10. PvP 시즌 테이블 (pvp_seasons)
-- =====================================================
CREATE TABLE pvp_seasons (
    id INT PRIMARY KEY AUTO_INCREMENT,
    season_name_en VARCHAR(50) NOT NULL,
    season_name_ko VARCHAR(50) NOT NULL,
    season_name_ja VARCHAR(50) NOT NULL,
    season_name_zh VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='PvP 시즌 정보';

-- =====================================================
-- 11. PvP 랭킹 테이블 (pvp_rankings)
-- =====================================================
CREATE TABLE pvp_rankings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    season_id INT NOT NULL,
    player_id BIGINT NOT NULL,
    elo INT NOT NULL DEFAULT 1000,
    wins INT NOT NULL DEFAULT 0,
    losses INT NOT NULL DEFAULT 0,
    draws INT NOT NULL DEFAULT 0,
    rank_tier ENUM('Bronze', 'Silver', 'Gold', 'Platinum', 'Diamond', 'Master') NOT NULL DEFAULT 'Bronze',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (season_id) REFERENCES pvp_seasons(id) ON DELETE CASCADE,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
    UNIQUE KEY uk_season_player (season_id, player_id),
    INDEX idx_elo (elo DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='PvP 시즌별 랭킹';

-- =====================================================
-- 12. 주사위 스킨 테이블 (dice_skins)
-- =====================================================
CREATE TABLE dice_skins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    skin_code VARCHAR(50) NOT NULL UNIQUE COMMENT '스킨 고유 코드',
    name_ko VARCHAR(100) NOT NULL,
    name_en VARCHAR(100) NOT NULL,
    name_ja VARCHAR(100) NOT NULL,
    name_zh VARCHAR(100) NOT NULL,
    description_ko TEXT NOT NULL,
    description_en TEXT NOT NULL,
    description_ja TEXT NOT NULL,
    description_zh TEXT NOT NULL,
    rarity ENUM('Common', 'Rare', 'Epic', 'Legendary') NOT NULL,
    price INT NOT NULL COMMENT '영혼석 가격 (0이면 기본 제공)',
    material VARCHAR(50) NOT NULL DEFAULT 'MeshStandardMaterial' COMMENT 'Three.js 재질 타입',
    base_color VARCHAR(7) NOT NULL COMMENT '주사위 기본 색상 (#RRGGBB)',
    pip_color VARCHAR(7) NOT NULL COMMENT '주사위 눈 색상 (#RRGGBB)',
    texture_url VARCHAR(255) COMMENT '텍스처 이미지 URL (선택)',
    metalness FLOAT DEFAULT 0.0 COMMENT '금속성 (0.0-1.0)',
    roughness FLOAT DEFAULT 0.5 COMMENT '거칠기 (0.0-1.0)',
    emissive_color VARCHAR(7) COMMENT '발광 색상 (#RRGGBB, 선택)',
    emissive_intensity FLOAT DEFAULT 0.0 COMMENT '발광 강도 (0.0-1.0)',
    is_default BOOLEAN DEFAULT FALSE COMMENT '기본 스킨 여부',
    is_available BOOLEAN DEFAULT TRUE COMMENT '구매 가능 여부',
    preview_url VARCHAR(255) COMMENT '미리보기 이미지 URL',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_rarity (rarity),
    INDEX idx_is_available (is_available),
    INDEX idx_skin_code (skin_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='주사위 스킨 정보 (코스메틱)';

-- =====================================================
-- 13. 아바타 테이블 (avatars)
-- =====================================================
CREATE TABLE avatars (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    avatar_code VARCHAR(50) NOT NULL UNIQUE COMMENT '아바타 고유 코드',
    name_ko VARCHAR(100) NOT NULL,
    name_en VARCHAR(100) NOT NULL,
    name_ja VARCHAR(100) NOT NULL,
    name_zh VARCHAR(100) NOT NULL,
    description_ko TEXT NOT NULL,
    description_en TEXT NOT NULL,
    description_ja TEXT NOT NULL,
    description_zh TEXT NOT NULL,
    rarity ENUM('Common', 'Rare', 'Epic', 'Legendary') NOT NULL,
    price INT NOT NULL COMMENT '영혼석 가격 (0이면 기본 제공)',
    avatar_url VARCHAR(255) NOT NULL COMMENT '아바타 이미지 URL',
    is_default BOOLEAN DEFAULT FALSE COMMENT '기본 아바타 여부',
    is_available BOOLEAN DEFAULT TRUE COMMENT '구매 가능 여부',
    preview_url VARCHAR(255) COMMENT '미리보기 이미지 URL',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_rarity (rarity),
    INDEX idx_is_available (is_available),
    INDEX idx_avatar_code (avatar_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='아바타 정보 (코스메틱)';

-- =====================================================
-- 14. 플레이어 코스메틱 소유 테이블 (player_cosmetics)
-- =====================================================
CREATE TABLE player_cosmetics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    player_id BIGINT NOT NULL,
    cosmetic_type ENUM('DICE_SKIN', 'AVATAR') NOT NULL,
    cosmetic_id BIGINT NOT NULL COMMENT '코스메틱 아이템 ID (dice_skins.id or avatars.id)',
    purchased_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
    UNIQUE KEY uk_player_cosmetic (player_id, cosmetic_type, cosmetic_id),
    INDEX idx_player_id (player_id),
    INDEX idx_cosmetic_type (cosmetic_type),
    INDEX idx_player_type (player_id, cosmetic_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='플레이어가 소유한 코스메틱 아이템';

-- =====================================================
-- 완료 메시지
-- =====================================================
SELECT 'Table creation complete! (14 tables)' AS message;
SHOW TABLES;
