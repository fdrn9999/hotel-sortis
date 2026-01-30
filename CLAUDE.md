# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

HOTEL SORTIS is a turn-based strategy roguelike game featuring a Chinchiro dice system with skill builds.
- **Genre**: Turn-based Strategy Roguelike / Skill Build PvP
- **Platform**: Web Browser (WebGL), Cross-platform (PC/Mobile)
- **Languages**: Korean, English, Japanese, Chinese (Simplified)

## Development Commands

### Frontend (Vue 3 + Vite)
```bash
cd frontend
npm install          # Install dependencies
npm run dev          # Start dev server (localhost:5173)
npm run build        # Build for production
npm run preview      # Preview production build
npm run lint         # Run ESLint
npm run format       # Run Prettier
```

### Backend (Spring Boot 3.2 + Gradle)
```bash
cd backend
./gradlew bootRun    # Start server (localhost:8080)
./gradlew build      # Build project
./gradlew test       # Run tests
./gradlew clean      # Clean build files
```

### Database Setup
```bash
# MariaDB 데이터베이스 생성
mysql -u root -p -e "CREATE DATABASE hotel_sortis CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 테이블 생성 및 초기 데이터
cd docs
mysql -u root -p hotel_sortis < create.sql
mysql -u root -p hotel_sortis < insert.sql
```
- **MariaDB**: `hotel_sortis` database
- **Redis**: Default port 6379
- **SQL Files**: `docs/create.sql` (DDL), `docs/insert.sql` (DML)

## Architecture

### Frontend (`/frontend`)
- **Framework**: Vue 3.4+ with Composition API (`<script setup>`)
- **State**: Pinia stores (`/src/stores`)
- **Routing**: Vue Router 4 (`/src/router`)
- **i18n**: Vue I18n 9+ (`/src/i18n/locales/{ko,en,ja,zh}.json`)
- **3D Dice**: Three.js + Cannon-es (`/src/game`)
- **WebSocket**: STOMP.js + SockJS (`/src/composables/useWebSocket.ts`)

### Backend (`/backend`)
- **Framework**: Spring Boot 3.2+, Java 17+
- **Database**: MariaDB 11.x + Spring Data JPA
- **Cache**: Redis 7.x
- **API**: Spring HATEOAS (RESTful)
- **WebSocket**: Spring WebSocket (STOMP) at `/ws`

### Core Game Logic
- Hand evaluation: `backend/src/main/java/com/hotelsortis/api/game/HandEvaluator.java`
- Frontend mirror: `frontend/src/stores/battle.ts` (evaluateHand function)

## i18n (다국어) 필수 규칙

### 지원 언어 (4개, 순서 고정)
| 코드 | 언어 | 용도 |
|------|------|------|
| `ko` | 한국어 | 기본 언어 |
| `en` | English | 글로벌 기본 |
| `ja` | 日本語 | 일본어 |
| `zh` | 简体中文 | 중국어 간체 |

### 규칙 1: DB 테이블 - 사용자에게 노출되는 텍스트는 반드시 4개국어 컬럼
```sql
-- ✅ 올바른 테이블 설계
CREATE TABLE skills (
    name_ko VARCHAR(100) NOT NULL,
    name_en VARCHAR(100) NOT NULL,
    name_ja VARCHAR(100) NOT NULL,
    name_zh VARCHAR(100) NOT NULL,
    description_ko TEXT NOT NULL,
    description_en TEXT NOT NULL,
    description_ja TEXT NOT NULL,
    description_zh TEXT NOT NULL
);

-- ❌ 잘못된 설계 (일부 언어 누락)
CREATE TABLE floors (
    description_ko VARCHAR(255),
    description_en VARCHAR(255)
    -- ja, zh 누락!
);
```

### 규칙 2: INSERT 문 - 모든 언어 컬럼에 데이터 필수
```sql
-- ✅ 올바른 INSERT
INSERT INTO skills (name_ko, name_en, name_ja, name_zh) VALUES
('행운의 재굴림', 'Lucky Reroll', 'ラッキーリロール', '幸运重掷');

-- ❌ 잘못된 INSERT (일부 언어 누락)
INSERT INTO skills (name_ko, name_en) VALUES
('행운의 재굴림', 'Lucky Reroll');
```

### 규칙 3: JSON 데이터 내 텍스트도 4개국어
```sql
-- ✅ 보스 대사 JSON: 모든 키에 _ko, _en, _ja, _zh 접미사
'{
  "intro_ko": "어서 오게, 방랑자여.",
  "intro_en": "Welcome, wanderer.",
  "intro_ja": "ようこそ、放浪者よ。",
  "intro_zh": "欢迎，流浪者。"
}'

-- ❌ 일부 언어 누락
'{
  "intro": "Welcome, wanderer.",
  "intro_ko": "어서 오게, 방랑자여."
}'
```

### 규칙 4: Frontend i18n JSON - 4개 파일 동일 키 구조 유지
- `frontend/src/i18n/locales/ko.json`
- `frontend/src/i18n/locales/en.json`
- `frontend/src/i18n/locales/ja.json`
- `frontend/src/i18n/locales/zh.json`

**새 키를 추가할 때 반드시 4개 파일 모두에 추가해야 합니다.**

### 규칙 5: 체크리스트 (새 기능/데이터 추가 시)
- [ ] DB 테이블에 `_ko`, `_en`, `_ja`, `_zh` 컬럼 존재?
- [ ] INSERT 문에 4개 언어 데이터 모두 포함?
- [ ] JSON 내 텍스트에 4개 언어 키 존재?
- [ ] Frontend i18n JSON 4개 파일에 동일 키 존재?
- [ ] Backend API 응답에서 사용자 언어에 맞는 텍스트 반환?

---

# HOTEL SORTIS 개발 준수 지침서
## Development Guidelines for Project Agents

**문서 목적**: 프로젝트 에이전트가 개발 시 반드시 준수해야 할 규칙과 표준을 정의합니다.

---

## 📋 필수 숙지 사항

### 🚨 즉시 거부해야 할 요청
다음 요청이 들어오면 **이유를 설명하고 즉시 거부**하세요:

1. ❌ 족보 시스템 변경 (공격력, 순위, 조건)
2. ❌ 스킬 슬롯 4개 초과 설정 (최대 4개 고정)
3. ❌ HP를 100 이외의 값으로 설정
4. ❌ Pay-to-Win 요소 추가 (스킬 구매, 확률 조작 등)
5. ❌ 동시 주사위 굴림 (턴제가 아닌 동시 진행)
6. ❌ 1920년대 세계관 벗어난 요소
7. ❌ 주사위 결과를 클라이언트에서 생성
8. ❌ alert(), confirm(), prompt() 등 브라우저 기본 팝업 사용
9. ❌ 홈/뒤로가기/설정 버튼 없는 화면

---

## 1. 핵심 개발 규칙 (절대 준수)

### 1.1 전투 시스템 구현 규칙

#### 1.1.1 턴제 시스템 (Turn-based)
**필수**: 플레이어와 적은 번갈아가며 행동합니다. 동시 실행 금지!

**턴 시간 제한**: 각 턴마다 30초 제한이 있습니다.

```javascript
// ✅ 올바른 턴제 구현 (시간제한 포함)
class BattleSystem {
  constructor() {
    this.currentTurn = 'player';
    this.turnCount = 1;
    this.maxTurns = 10;
    this.turnTimeLimit = 30000;  // 30초 (밀리초)
    this.turnTimer = null;
  }

  async executeTurn() {
    // 턴 시작 시 타이머 시작
    this.startTurnTimer();
    
    if (this.currentTurn === 'player') {
      await this.playerTurn();
      this.currentTurn = 'enemy';
    } else {
      await this.enemyTurn();
      this.currentTurn = 'player';
      this.turnCount++;
    }
    
    // 턴 종료 시 타이머 정리
    this.clearTurnTimer();
    
    await this.checkBattleEnd();
  }

  startTurnTimer() {
    this.turnTimer = setTimeout(() => {
      this.onTurnTimeout();
    }, this.turnTimeLimit);
    
    // UI에 타이머 표시
    this.updateTimerUI(this.turnTimeLimit);
  }

  clearTurnTimer() {
    if (this.turnTimer) {
      clearTimeout(this.turnTimer);
      this.turnTimer = null;
    }
  }

  onTurnTimeout() {
    // 시간 초과 시 처리
    if (this.currentTurn === 'player') {
      // 플레이어 턴 타임아웃: 자동으로 주사위 굴림
      this.autoRollDice();
    } else {
      // AI 턴 타임아웃: 즉시 행동 (버그 방지)
      console.error('AI turn timeout - should not happen');
      this.enemyTurn();
    }
  }

  async playerTurn() {
    // 1. 스킬 발동 (BATTLE_START는 첫 턴만)
    if (this.turnCount === 1) {
      await this.triggerSkills('BATTLE_START', 'player');
    }
    
    // 2. 주사위 굴림 대기 (사용자 입력 또는 타임아웃)
    const dice = await this.waitForPlayerAction();
    
    // 3. 스킬 효과 적용
    await this.triggerSkills('DICE_ROLL', 'player', dice);
    
    // 4. 족보 판정
    const hand = this.evaluateHand(dice);
    
    // 5. 데미지 계산 및 처리
    const damage = hand.power;
    await this.dealDamage('enemy', damage);
    
    // 6. 애니메이션 대기
    await this.waitForAnimation(2000);
  }

  async waitForPlayerAction() {
    // 사용자가 주사위를 굴릴 때까지 대기
    // 또는 타임아웃 시 자동으로 굴림
    return new Promise((resolve) => {
      this.playerActionResolver = resolve;
    });
  }

  autoRollDice() {
    // 타임아웃 시 자동 주사위 굴림
    if (this.playerActionResolver) {
      const dice = this.rollDiceOnServer();
      this.playerActionResolver(dice);
      this.playerActionResolver = null;
      
      // UI에 타임아웃 알림
      this.showTimeoutNotification();
    }
  }

  async enemyTurn() {
    // 적도 동일한 순서로 진행
    // AI는 즉시 행동하므로 타임아웃 걱정 없음
    // ...
  }
}

// ❌ 잘못된 구현 (동시 실행)
async function battle() {
  const [playerRoll, enemyRoll] = await Promise.all([
    rollDice('player'),
    rollDice('enemy')  // 동시에 굴림 - 절대 금지!
  ]);
}
```

**턴 타이머 UI 구현 (Vue 3)**:
```vue
<template>
  <div class="turn-timer">
    <div class="timer-bar" :style="{ width: timerPercentage + '%' }"></div>
    <span class="timer-text">{{ timeRemaining }}s</span>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';

const turnTimeLimit = 30; // 30초
const timeRemaining = ref(turnTimeLimit);
const timerInterval = ref(null);

const timerPercentage = computed(() => {
  return (timeRemaining.value / turnTimeLimit) * 100;
});

const startTimer = () => {
  timeRemaining.value = turnTimeLimit;
  
  timerInterval.value = setInterval(() => {
    timeRemaining.value--;
    
    if (timeRemaining.value <= 0) {
      clearInterval(timerInterval.value);
      emit('timeout');
    }
  }, 1000);
};

const stopTimer = () => {
  if (timerInterval.value) {
    clearInterval(timerInterval.value);
    timerInterval.value = null;
  }
};

defineExpose({ startTimer, stopTimer });

onUnmounted(() => {
  stopTimer();
});
</script>

<style scoped>
.turn-timer {
  position: relative;
  width: 200px;
  height: 30px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 15px;
  overflow: hidden;
}

.timer-bar {
  height: 100%;
  background: linear-gradient(90deg, #4CAF50, #FFC107, #F44336);
  transition: width 1s linear;
}

.timer-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  font-weight: bold;
  font-size: 16px;
}
</style>
```

**서버 측 타임아웃 처리 (Spring Boot)**:
```java
@Service
@RequiredArgsConstructor
public class TurnTimerService {
    
    private final BattleService battleService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final Map<Long, ScheduledFuture<?>> turnTimers = new ConcurrentHashMap<>();
    
    // 턴 시작 시 타이머 시작
    public void startTurnTimer(Long battleId, Long playerId) {
        // 기존 타이머 취소
        cancelTurnTimer(battleId);
        
        // 30초 후 자동 실행
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            handleTurnTimeout(battleId, playerId);
        }, 30, TimeUnit.SECONDS);
        
        turnTimers.put(battleId, future);
    }
    
    // 턴 종료 시 타이머 취소
    public void cancelTurnTimer(Long battleId) {
        ScheduledFuture<?> future = turnTimers.remove(battleId);
        if (future != null && !future.isDone()) {
            future.cancel(false);
        }
    }
    
    // 타임아웃 처리
    private void handleTurnTimeout(Long battleId, Long playerId) {
        try {
            // 자동으로 주사위 굴림
            battleService.autoRollDice(battleId, playerId);
            
            log.warn("Turn timeout for battle {} player {}", battleId, playerId);
        } catch (Exception e) {
            log.error("Error handling turn timeout", e);
        } finally {
            turnTimers.remove(battleId);
        }
    }
}

@Service
@RequiredArgsConstructor
public class BattleService {
    
    private final TurnTimerService turnTimerService;
    
    @Transactional
    public BattleActionResultDto rollDice(Long battleId, Long playerId) {
        // 타이머 취소
        turnTimerService.cancelTurnTimer(battleId);
        
        // 주사위 굴림 처리
        // ...
        
        // 다음 턴 타이머 시작 (적 턴이면 타이머 불필요)
        Battle battle = battleRepository.findById(battleId).orElseThrow();
        if (battle.getCurrentTurn().equals("player")) {
            turnTimerService.startTurnTimer(battleId, playerId);
        }
        
        return result;
    }
    
    @Transactional
    public BattleActionResultDto autoRollDice(Long battleId, Long playerId) {
        // 타임아웃 시 자동 주사위 굴림
        log.info("Auto-rolling dice for battle {} player {} (timeout)", battleId, playerId);
        
        // 일반 주사위 굴림과 동일하게 처리
        return rollDice(battleId, playerId);
    }
}
```

**체크리스트**:
- [ ] 플레이어 턴마다 30초 타이머 시작
- [ ] 타이머 UI에 남은 시간 표시
- [ ] 타임아웃 시 자동으로 주사위 굴림
- [ ] 주사위 굴림 시 타이머 취소
- [ ] 적 턴에는 타이머 없음 (즉시 행동)
- [ ] 서버에서도 타임아웃 검증

#### 1.1.2 HP 시스템 (고정 100)
**필수**: 모든 캐릭터의 HP는 100으로 고정합니다.

```javascript
// ✅ 올바른 HP 설정
const createCharacter = (type) => ({
  hp: 100,  // 항상 100
  maxHp: 100,  // 항상 100
  type: type,
  // ...
});

// ❌ 잘못된 HP 설정
const createEnemy = (floor) => ({
  hp: floor <= 5 ? 50 : 150,  // 층별 HP 변경 - 금지!
  maxHp: floor * 10,  // 최대 HP 변경 - 금지!
});
```

**난이도 조절 방법**:
```javascript
// ✅ AI 레벨로 난이도 조절
function getEnemyConfig(floor) {
  return {
    hp: 100,  // 고정
    aiLevel: floor <= 3 ? 0 : floor <= 9 ? 1 : floor <= 14 ? 2 : 3,
    skills: getSkillsByFloor(floor),
    restrictions: floor === 15 ? ['use_6_dice'] : []
  };
}

// AI 레벨별 행동
function enemyAI(aiLevel, gameState) {
  switch(aiLevel) {
    case 0:  // BASIC (1-3층)
      return randomMove(gameState);
    case 1:  // STANDARD (4-9층)
      return basicStrategy(gameState);
    case 2:  // ADVANCED (10-14층)
      return predictiveStrategy(gameState);
    case 3:  // MASTER (15층)
      return optimalStrategy(gameState);
  }
}
```

#### 1.1.3 족보 시스템 (Balanced System)
**필수**: 족보 공식을 정확히 구현하세요.

```javascript
// ✅ 올바른 족보 판정 (Balanced System)
function evaluateHand(dice) {
  const sorted = [...dice].sort();
  const [a, b, c] = sorted;

  // Ace: [1-1-1] -> 45 DMG
  if (a === 1 && b === 1 && c === 1) {
    return { rank: 'Ace', power: 45 };
  }

  // Triple: 동일 3개 [2-6] -> 8 + (N*4)
  if (a === b && b === c && a >= 2) {
    return { rank: 'Triple', power: 8 + (a * 4) };
  }

  // Straight: [4-5-6] -> 38 DMG
  if (a === 4 && b === 5 && c === 6) {
    return { rank: 'Straight', power: 38 };
  }

  // Strike: [3-4-5] -> 30 DMG
  if (a === 3 && b === 4 && c === 5) {
    return { rank: 'Strike', power: 30 };
  }

  // Slash: [2-3-4] -> 24 DMG
  if (a === 2 && b === 3 && c === 4) {
    return { rank: 'Slash', power: 24 };
  }

  // Storm: [1-2-3] -> 16 DMG
  if (a === 1 && b === 2 && c === 3) {
    return { rank: 'Storm', power: 16 };
  }
  
  // Pair: 동일 2개 -> 5 + (N*2)
  if (a === b || b === c) {
    const pairValue = a === b ? a : b;
    return { rank: 'Pair', power: 5 + (pairValue * 2) };
  }
  
  // No Hand: 합계
  return { rank: 'NoHand', power: a + b + c };
}

// ❌ 이런 수정 절대 금지!
function evaluateHand(dice) {
  // "Ace가 너무 강해서 150으로 약화"
  if (isAce(dice)) return { rank: 'Ace', power: 150 };  // 절대 안됨!
  
  // "새로운 족보 추가"
  if (isFullHouse(dice)) return { rank: 'FullHouse', power: 200 };  // 금지!
}
```

#### 1.1.4 주사위 굴림 (서버 필수)
**필수**: 주사위 결과는 반드시 서버에서 생성합니다.

```javascript
// ✅ 올바른 구현 (서버 생성)
async function rollDice(playerId) {
  const response = await fetch('/api/battle/roll', {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}` },
    body: JSON.stringify({ playerId })
  });
  
  const { dice, hash } = await response.json();
  // dice: [3, 5, 2] - 서버에서 생성됨
  // hash: "abc123..." - 변조 방지 해시
  
  return dice;
}

// ❌ 잘못된 구현 (클라이언트 생성)
function rollDice() {
  return [
    Math.floor(Math.random() * 6) + 1,
    Math.floor(Math.random() * 6) + 1,
    Math.floor(Math.random() * 6) + 1
  ];  // 클라이언트에서 생성 - 치팅 가능! 절대 금지!
}
```

**서버 측 구현**:
```javascript
// server/battle.js
app.post('/api/battle/roll', authenticateToken, (req, res) => {
  const { playerId } = req.body;
  
  // 서버에서 랜덤 생성
  const dice = [
    Math.floor(Math.random() * 6) + 1,
    Math.floor(Math.random() * 6) + 1,
    Math.floor(Math.random() * 6) + 1
  ];
  
  // 변조 방지 해시
  const hash = crypto
    .createHash('sha256')
    .update(`${playerId}-${dice.join('-')}-${Date.now()}`)
    .digest('hex');
  
  // DB에 기록 (검증용)
  saveBattleLog(playerId, dice, hash);
  
  res.json({ dice, hash });
});
```

### 1.2 스킬 시스템 구현 규칙

#### 1.2.1 스킬 슬롯 (최대 4개)
**필수**: 플레이어는 최대 4개의 스킬을 장착할 수 있습니다. (0~4개 가능)

```javascript
// ✅ 올바른 스킬 장착 시스템
class SkillManager {
  constructor() {
    this.MAX_SLOTS = 4;  // 상수로 고정
    this.equippedSkills = [null, null, null, null];  // 4개 슬롯, 빈 상태로 시작
  }

  equipSkill(skill, slotIndex) {
    if (slotIndex < 0 || slotIndex >= this.MAX_SLOTS) {
      throw new Error(`Invalid slot index. Must be 0-${this.MAX_SLOTS - 1}`);
    }
    
    // 이미 다른 슬롯에 장착되어 있는지 확인
    const existingIndex = this.equippedSkills.findIndex(s => s?.id === skill.id);
    if (existingIndex !== -1 && existingIndex !== slotIndex) {
      // 기존 슬롯에서 제거
      this.equippedSkills[existingIndex] = null;
    }
    
    // 슬롯에 스킬 장착
    this.equippedSkills[slotIndex] = skill;
  }

  unequipSkill(slotIndex) {
    if (slotIndex < 0 || slotIndex >= this.MAX_SLOTS) {
      throw new Error(`Invalid slot index. Must be 0-${this.MAX_SLOTS - 1}`);
    }
    
    this.equippedSkills[slotIndex] = null;
  }

  validateLoadout() {
    // 장착된 스킬 수 확인 (null 제외)
    const equipped = this.equippedSkills.filter(s => s !== null);
    
    // 0~4개 사이여야 함
    if (equipped.length > 4) {
      throw new Error('Cannot equip more than 4 skills');
    }
    
    // 중복 체크
    const ids = equipped.map(s => s.id);
    const unique = new Set(ids);
    if (ids.length !== unique.size) {
      throw new Error('Duplicate skills detected');
    }
    
    return true;
  }

  getEquippedCount() {
    return this.equippedSkills.filter(s => s !== null).length;
  }

  canStartBattle() {
    // 0개여도 전투 시작 가능 (스킬 없이도 플레이 가능)
    return this.validateLoadout();
  }
}

// ❌ 이런 코드 절대 금지!
class SkillManager {
  constructor() {
    this.slots = 4;  // 변수로 설정 - 위험!
  }

  upgradeSlots() {
    this.slots += 1;  // 슬롯 증가 - 절대 금지!
  }

  equipSkill(skill) {
    if (this.equippedSkills.length < this.slots) {
      this.equippedSkills.push(skill);  // 가변 슬롯 - 금지!
    }
  }
}
```

**게임 진행 예시**:
```javascript
// 게임 시작 (스킬 0개)
const player = new Player();
console.log(player.skillManager.getEquippedCount()); // 0

// 첫 보스 클리어 후 스킬 1개 획득
player.unlockSkill(luckyReroll);
player.skillManager.equipSkill(luckyReroll, 0);
console.log(player.skillManager.getEquippedCount()); // 1

// 두 번째 보스 클리어 후 스킬 2개 추가
player.unlockSkill(highRoller);
player.skillManager.equipSkill(highRoller, 1);
console.log(player.skillManager.getEquippedCount()); // 2

// 최종적으로 4개까지 장착 가능
player.skillManager.equipSkill(stormSeeker, 2);
player.skillManager.equipSkill(perfectDefense, 3);
console.log(player.skillManager.getEquippedCount()); // 4
```

#### 1.2.2 스킬 효과 작성 규칙
**필수**: 스킬은 명확하고 테스트 가능하게 작성합니다.

```javascript
// ✅ 올바른 스킬 정의
const LuckyReroll = {
  id: 'lucky_reroll',
  name: 'Lucky Reroll',
  nameKR: '행운의 재굴림',
  rarity: 'Common',
  description: '매 전투 시작 시 주사위 1개를 자동으로 재굴림합니다.',
  
  // 명확한 트리거와 효과
  trigger: 'BATTLE_START',
  effect: function(gameState) {
    // 주사위 1개 재굴림
    const rerollIndex = 0;  // 첫 번째 주사위
    gameState.dice[rerollIndex] = rollSingleDie();
    
    // 로그 기록 (디버깅용)
    log('LuckyReroll', `Rerolled dice[0]: ${gameState.dice[rerollIndex]}`);
    
    return gameState;
  },
  
  // 단위 테스트 가능
  test: function() {
    const state = { dice: [1, 2, 3] };
    const result = this.effect(state);
    assert(result.dice[0] >= 1 && result.dice[0] <= 6);
  }
};

// ❌ 잘못된 스킬 정의
const BadSkill = {
  name: "Lucky Skill",
  description: "행운을 증가시킵니다",  // 모호함!
  effect: function(state) {
    state.luck += 1;  // "luck" 스탯이 없음!
    // 로그 없음, 테스트 불가능
  }
};
```

**스킬 트리거 타입**:
```javascript
const SKILL_TRIGGERS = {
  BATTLE_START: 'BATTLE_START',      // 전투 시작 시 1회
  TURN_START: 'TURN_START',          // 매 턴 시작 시
  DICE_ROLL: 'DICE_ROLL',            // 주사위 굴린 직후
  BEFORE_DAMAGE: 'BEFORE_DAMAGE',    // 데미지 계산 전
  AFTER_DAMAGE: 'AFTER_DAMAGE',      // 데미지 처리 후
  PASSIVE: 'PASSIVE'                 // 항상 적용
};

// 스킬 작성 시 반드시 위 타입 중 하나 사용
```

### 1.3 Pay-to-Win 금지 규칙

#### 1.3.1 절대 금지 목록
**필수**: 다음 요소를 구매 가능하게 만들면 즉시 거부하세요.

```javascript
// ❌ 절대 금지되는 코드 패턴들

// 1. 스킬 구매
function purchaseSkill(skillId, price) {
  // 이런 함수 자체가 존재하면 안 됨!
}

// 2. 확률 조작 아이템
function buyRerolls(count, price) {
  player.rerollCount += count;  // 금지!
}

// 3. 스탯 부스터
function buyAttackBoost(price) {
  player.attackMultiplier += 0.1;  // 금지!
}

// 4. 경험치 부스터
function buyXPBoost(duration, price) {
  player.xpMultiplier = 2.0;  // 금지!
}

// 5. 부활 아이템
function buyRevive(price) {
  if (player.hp <= 0) {
    player.hp = 100;  // 금지!
  }
}
```

#### 1.3.2 허용되는 구매 (코스메틱만)
```javascript
// ✅ 허용되는 코드 (외형 변경만)

// 주사위 스킨
function purchaseDiceSkin(skinId, price) {
  const skin = DICE_SKINS[skinId];
  
  // 외형만 변경, 게임플레이 영향 없음
  player.cosmetics.diceSkin = {
    model: skin.model,      // 3D 모델
    texture: skin.texture,  // 텍스처
    material: skin.material // 재질
  };
  
  // 주사위 결과에는 영향 없음!
  assert(skin.doesNotAffectGameplay === true);
}

// 아바타 스킨
function purchaseAvatar(avatarId, price) {
  player.cosmetics.avatar = AVATARS[avatarId];
  // 외형만 변경
}

// BGM 팩
function purchaseBGM(bgmId, price) {
  player.settings.customBGM = BGM_PACKS[bgmId];
  // 음악만 변경
}
```

**코드 리뷰 시 체크사항**:
```javascript
// 새로운 구매 기능 추가 시 반드시 확인
function validatePurchase(item) {
  // 1. 외형 변경인가?
  if (!item.isCosmeticOnly) {
    throw new Error('Non-cosmetic purchase detected!');
  }
  
  // 2. 게임플레이에 영향 주는가?
  if (item.affectsGameplay) {
    throw new Error('Pay-to-win element detected!');
  }
  
  // 3. 경쟁 우위를 주는가?
  if (item.providesAdvantage) {
    throw new Error('Competitive advantage detected!');
  }
  
  return true;
}
```

---

## 2. 핵심 시스템 구현 가이드

### 2.1 AI 시스템 (난이도 조절)

#### 2.1.1 AI 레벨별 구현
**필수**: HP 대신 AI 지능으로 난이도를 조절합니다.

```javascript
// AI 레벨 정의
const AI_LEVELS = {
  BASIC: 0,      // 1-3층: 완전 랜덤
  STANDARD: 1,   // 4-9층: 기본 전략
  ADVANCED: 2,   // 10-14층: 예측 및 최적화
  MASTER: 3      // 15층: 최고 난이도
};

// ✅ AI 레벨별 구현
class EnemyAI {
  constructor(level) {
    this.level = level;
  }

  // Level 0: BASIC - 완전 랜덤
  basicAI(gameState) {
    // 스킬 사용 안 함
    // 주사위 굴리기만 함
    return {
      action: 'ROLL',
      useSkill: null
    };
  }

  // Level 1: STANDARD - 간단한 전략
  standardAI(gameState) {
    const { playerHP, enemyHP, turn } = gameState;
    
    // 기본 전략: HP 낮으면 방어 스킬 사용
    if (enemyHP < 30 && this.hasDefensiveSkill()) {
      return {
        action: 'USE_SKILL',
        skillId: this.getDefensiveSkill()
      };
    }
    
    return { action: 'ROLL' };
  }

  // Level 2: ADVANCED - 족보 예측
  advancedAI(gameState) {
    const { playerDiceHistory, playerSkills } = gameState;
    
    // 플레이어 패턴 분석
    const predictedHand = this.predictPlayerHand(playerDiceHistory);
    
    // 카운터 전략
    if (predictedHand.power > 100) {
      // 강력한 공격 예상 → 방어
      return this.useDefensiveStrategy();
    } else {
      // 약한 공격 예상 → 공격
      return this.useOffensiveStrategy();
    }
  }

  // Level 3: MASTER - 최적 플레이
  masterAI(gameState) {
    // 모든 가능한 경우의 수 계산
    const outcomes = this.simulateAllOutcomes(gameState);
    
    // 승률 가장 높은 행동 선택
    const bestAction = outcomes.reduce((best, current) => 
      current.winRate > best.winRate ? current : best
    );
    
    return bestAction;
  }

  // AI 레벨에 따라 적절한 함수 호출
  decideAction(gameState) {
    switch(this.level) {
      case AI_LEVELS.BASIC:
        return this.basicAI(gameState);
      case AI_LEVELS.STANDARD:
        return this.standardAI(gameState);
      case AI_LEVELS.ADVANCED:
        return this.advancedAI(gameState);
      case AI_LEVELS.MASTER:
        return this.masterAI(gameState);
      default:
        throw new Error(`Invalid AI level: ${this.level}`);
    }
  }
}
```

#### 2.1.2 보스별 특수 능력 및 페이즈
```javascript
// ✅ 보스별 페이즈 및 특수 능력
const BOSS_CONFIGS = {
  'mammon': {
    floor: 5,
    name: 'Mammon',
    phases: 2,  // 2페이즈 (총 HP 200)
    
    phase1: {
      hp: 100,
      aiLevel: AI_LEVELS.STANDARD,
      pattern: 'aggressive',
      skills: ['greed_dice', 'double_or_nothing'],
      restrictions: {
        bannedHands: ['Storm'],  // Storm 사용 불가
        riskReward: true         // 높은 리스크/리워드
      },
      description: '탐욕의 공격 패턴'
    },
    
    phase2: {
      hp: 100,
      aiLevel: AI_LEVELS.ADVANCED,
      pattern: 'desperate',
      skills: ['greed_dice', 'double_or_nothing', 'all_or_nothing'],
      restrictions: {
        bannedHands: [],         // 제한 해제
        riskReward: true,
        desperateMode: true      // 극단적 베팅
      },
      description: '절망적인 도박'
    }
  },
  
  'eligor': {
    floor: 10,
    name: 'Eligor',
    phases: 2,  // 2페이즈 (총 HP 200)
    
    phase1: {
      hp: 100,
      aiLevel: AI_LEVELS.ADVANCED,
      pattern: 'defensive',
      skills: ['perfect_defense', 'armor_up'],
      restrictions: {
        damageReduction: 0.2,    // 받는 데미지 20% 감소
        counterAttack: false
      },
      description: '완벽한 방어'
    },
    
    phase2: {
      hp: 100,
      aiLevel: AI_LEVELS.ADVANCED,
      pattern: 'counter',
      skills: ['perfect_defense', 'counter_attack', 'armor_up'],
      restrictions: {
        damageReduction: 0.3,    // 받는 데미지 30% 감소
        counterAttack: true,     // 공격받으면 반격
        counterMultiplier: 0.5   // 받은 데미지의 50% 반격
      },
      description: '반격의 갑옷'
    }
  },
  
  'lucifuge': {
    floor: 15,
    name: 'Lucifuge Rofocale',
    phases: 3,  // 3페이즈 (총 HP 300)
    
    phase1: {
      hp: 100,
      aiLevel: AI_LEVELS.MASTER,
      pattern: 'testing',
      skills: ['dimensional_dice', 'fate_manipulation'],
      restrictions: {
        diceCount: 6,            // 6개 주사위
        specialDice: []          // 일반 주사위만
      },
      description: '운명의 시험'
    },
    
    phase2: {
      hp: 100,
      aiLevel: AI_LEVELS.MASTER,
      pattern: 'serious',
      skills: ['dimensional_dice', 'fate_manipulation', 'probability_distortion'],
      restrictions: {
        diceCount: 7,            // 7개 주사위
        specialDice: ['void', 'chaos']  // 특수 주사위 2종
      },
      description: '진지한 심판'
    },
    
    phase3: {
      hp: 100,
      aiLevel: AI_LEVELS.MASTER,
      pattern: 'true_form',
      skills: ['dimensional_dice', 'fate_manipulation', 'probability_distortion', 'cosmic_hand'],
      restrictions: {
        diceCount: 9,            // 9개 주사위
        specialDice: ['void', 'chaos', 'destiny', 'paradox', 'infinity'],
        transformedForm: true    // 진형 변환
      },
      description: '운명의 화신'
    }
  }
};

// 페이즈 전환 시스템
class BossPhaseManager {
  constructor(bossId) {
    this.config = BOSS_CONFIGS[bossId];
    this.currentPhase = 1;
    this.hp = 100;
  }

  // 페이즈 전환 체크
  checkPhaseTransition() {
    if (this.hp <= 0 && this.currentPhase < this.config.phases) {
      this.transitionToNextPhase();
      return true;
    } else if (this.hp <= 0 && this.currentPhase === this.config.phases) {
      this.onBossDefeated();
      return false;
    }
    return false;
  }

  // 다음 페이즈로 전환
  async transitionToNextPhase() {
    this.currentPhase++;
    
    // 페이즈 전환 컷신
    await this.playPhaseTransitionCutscene();
    
    // HP 리셋
    this.hp = 100;
    
    // 새로운 페이즈 설정 적용
    const phaseConfig = this.config[`phase${this.currentPhase}`];
    this.applyPhaseConfig(phaseConfig);
    
    // UI 업데이트
    this.updatePhaseUI();
  }

  // 페이즈 전환 컷신
  async playPhaseTransitionCutscene() {
    // 1초 페이드 아웃
    await fadeOut(1000);
    
    // 보스 대사 (페이즈별)
    const quotes = {
      2: "Phase 2 activated...",
      3: "You've forced my hand. Witness true power."
    };
    
    await showBossQuote(quotes[this.currentPhase]);
    
    // 보스 변신 애니메이션
    await playTransformAnimation(this.currentPhase);
    
    // 1초 페이드 인
    await fadeIn(1000);
  }

  // 페이즈 설정 적용
  applyPhaseConfig(phaseConfig) {
    this.aiLevel = phaseConfig.aiLevel;
    this.skills = phaseConfig.skills;
    this.restrictions = phaseConfig.restrictions;
    this.pattern = phaseConfig.pattern;
  }

  // 보스 격파
  onBossDefeated() {
    playVictoryAnimation();
    unlockSkillReward(this.config.floor);
    saveProgress(this.config.floor);
  }
}
```

**페이즈 전환 UI 표시**:
```javascript
// 보스 HP 바 UI (페이즈 표시)
<div class="boss-hp-container">
  <div class="boss-name">Lucifuge Rofocale</div>
  
  <!-- 페이즈 인디케이터 -->
  <div class="phase-indicator">
    <span class="phase-dot active"></span>  <!-- 현재 페이즈 -->
    <span class="phase-dot active"></span>  <!-- 완료된 페이즈 -->
    <span class="phase-dot"></span>         <!-- 남은 페이즈 -->
  </div>
  
  <!-- 현재 페이즈 HP -->
  <div class="hp-bar">
    <div class="hp-fill" style="width: 65%">65 / 100</div>
  </div>
  
  <div class="phase-name">Phase 2: Serious</div>
</div>
```

### 2.2 스킬 획득 및 관리 시스템

#### 2.2.1 스킬 획득 (보스 클리어)
```javascript
// ✅ 보스 클리어 시 스킬 선택
async function onBossDefeated(bossFloor) {
  // 보스 층에 따라 스킬 등급 결정
  const rarity = getSkillRarityByFloor(bossFloor);
  
  // 해당 등급의 미획득 스킬 중 3개 랜덤 선택
  const availableSkills = getUnlockedSkills(rarity)
    .filter(skill => !player.unlockedSkills.includes(skill.id));
  
  const offered = randomSelect(availableSkills, 3);
  
  // 플레이어에게 선택 UI 표시
  const selected = await showSkillSelection(offered);
  
  // 선택한 스킬 영구 언락
  player.unlockedSkills.push(selected.id);
  
  // 선택하지 않은 스킬도 영구 언락
  offered.forEach(skill => {
    if (skill.id !== selected.id) {
      player.unlockedSkills.push(skill.id);
    }
  });
  
  // 다음 런에서 사용 가능
  savePlayerData(player);
}

function getSkillRarityByFloor(floor) {
  if (floor === 5) return 'Rare';
  if (floor === 10) return 'Epic';
  if (floor === 15) return 'Legendary';
  throw new Error(`Invalid boss floor: ${floor}`);
}
```

#### 2.2.2 스킬 장착 시스템
```javascript
// ✅ 스킬 장착 UI (런 시작 전 / 보스 클리어 후)
class SkillLoadoutUI {
  constructor() {
    this.slots = [null, null, null, null];  // 4개 슬롯
  }

  // 스킬 장착
  equipSkill(skill, slotIndex) {
    if (slotIndex < 0 || slotIndex >= 4) {
      throw new Error('Invalid slot index');
    }
    
    // 이미 다른 슬롯에 장착되어 있는지 확인
    const existingIndex = this.slots.findIndex(s => s?.id === skill.id);
    if (existingIndex !== -1 && existingIndex !== slotIndex) {
      // 기존 슬롯에서 제거
      this.slots[existingIndex] = null;
    }
    
    this.slots[slotIndex] = skill;
    this.validateLoadout();
  }

  // 스킬 제거
  unequipSkill(slotIndex) {
    if (slotIndex < 0 || slotIndex >= 4) {
      throw new Error('Invalid slot index');
    }
    
    this.slots[slotIndex] = null;
    this.validateLoadout();
  }

  // 장착 검증
  validateLoadout() {
    const equipped = this.slots.filter(s => s !== null);
    
    // 최대 4개까지만 가능
    if (equipped.length > 4) {
      throw new Error('Cannot equip more than 4 skills');
    }
    
    // 중복 체크
    const ids = equipped.map(s => s.id);
    const unique = new Set(ids);
    if (ids.length !== unique.size) {
      throw new Error('Duplicate skills detected');
    }
    
    return true;
  }

  // 장착 완료 (0~4개 모두 가능)
  confirmLoadout() {
    if (!this.validateLoadout()) {
      return false;
    }
    
    // 게임 시작 (스킬 0개여도 가능)
    const equippedCount = this.slots.filter(s => s !== null).length;
    console.log(`Starting with ${equippedCount} skills equipped`);
    
    startBattle(this.slots);
    return true;
  }

  // 장착된 스킬 수
  getEquippedCount() {
    return this.slots.filter(s => s !== null).length;
  }
}
```

**Vue 3 스킬 장착 UI**:
```vue
<template>
  <div class="skill-loadout">
    <h2>스킬 장착 ({{ equippedCount }}/4)</h2>
    
    <!-- 4개 슬롯 -->
    <div class="skill-slots">
      <div 
        v-for="(skill, index) in slots" 
        :key="index"
        class="skill-slot"
        :class="{ empty: !skill }"
        @click="onSlotClick(index)"
      >
        <div v-if="skill" class="skill-card">
          <img :src="skill.icon" :alt="skill.name" />
          <span class="skill-name">{{ skill.nameKR }}</span>
          <button class="unequip-btn" @click.stop="unequipSkill(index)">
            ✕
          </button>
        </div>
        <div v-else class="empty-slot">
          <span>빈 슬롯</span>
        </div>
      </div>
    </div>
    
    <!-- 보유 스킬 목록 -->
    <div class="owned-skills">
      <h3>보유 스킬</h3>
      <div class="skill-list">
        <div 
          v-for="skill in ownedSkills" 
          :key="skill.id"
          class="skill-item"
          :class="{ equipped: isEquipped(skill) }"
          @click="equipSkill(skill)"
        >
          <img :src="skill.icon" :alt="skill.name" />
          <span>{{ skill.nameKR }}</span>
        </div>
      </div>
    </div>
    
    <!-- 시작 버튼 (스킬 0개여도 가능) -->
    <button class="start-btn" @click="startBattle">
      전투 시작 ({{ equippedCount }}개 장착)
    </button>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const slots = ref([null, null, null, null]);
const ownedSkills = ref([]);  // 보유 중인 스킬 목록

const equippedCount = computed(() => {
  return slots.value.filter(s => s !== null).length;
});

const equipSkill = (skill) => {
  // 이미 장착된 경우 제거
  const existingIndex = slots.value.findIndex(s => s?.id === skill.id);
  if (existingIndex !== -1) {
    slots.value[existingIndex] = null;
    return;
  }
  
  // 빈 슬롯에 장착
  const emptyIndex = slots.value.findIndex(s => s === null);
  if (emptyIndex !== -1) {
    slots.value[emptyIndex] = skill;
  } else {
    // 슬롯 가득 참
    showModal({
      type: 'info',
      title: '슬롯 가득 찬',
      message: '최대 4개까지 장착할 수 있습니다. 스킬을 제거하고 다시 시도하세요.',
      buttons: [{ text: '확인', action: closeModal }]
    });
  }
};

const unequipSkill = (index) => {
  slots.value[index] = null;
};

const isEquipped = (skill) => {
  return slots.value.some(s => s?.id === skill.id);
};

const startBattle = () => {
  // 0~4개 모두 전투 시작 가능
  router.push({
    name: 'Battle',
    params: { skills: slots.value.filter(s => s !== null) }
  });
};
</script>
```

---

#### 2.2.2 HP 및 난이도 시스템

**⚠️ 중요: 모든 캐릭터의 HP는 100으로 고정입니다!**

```
플레이어 HP: 100 (고정, 전투당)
일반 몬스터 HP: 100 (고정)
엘리트 몬스터 HP: 100 (고정)
보스 HP: 페이즈당 100 (페이즈 시스템)
```

**보스 페이즈 시스템**:
보스는 여러 페이즈를 가지며, 각 페이즈마다 HP 100으로 시작합니다.

```javascript
// 보스 페이즈 구조
const BOSS_PHASES = {
  MAMMON: {
    floor: 5,
    phases: 2,  // 2페이즈 (HP 100 × 2 = 총 200)
    phase1: { hp: 100, pattern: 'aggressive' },
    phase2: { hp: 100, pattern: 'desperate' }
  },
  
  ELIGOR: {
    floor: 10,
    phases: 2,  // 2페이즈 (HP 100 × 2 = 총 200)
    phase1: { hp: 100, pattern: 'defensive' },
    phase2: { hp: 100, pattern: 'counter' }
  },
  
  LUCIFUGE: {
    floor: 15,
    phases: 3,  // 3페이즈 (HP 100 × 3 = 총 300)
    phase1: { hp: 100, pattern: 'testing' },
    phase2: { hp: 100, pattern: 'serious' },
    phase3: { hp: 100, pattern: 'true_form' }
  }
};
```

**페이즈 전환 규칙**:
- 현재 페이즈 HP가 0이 되면 다음 페이즈로 전환
- 페이즈 전환 시 HP가 100으로 리셋
- 페이즈마다 다른 AI 패턴, 스킬, 제한 적용

**난이도 조절 방식**:
HP가 아닌 다음 요소로 난이도를 조절합니다:

1. **페이즈 수 (Boss Phase Count)**
   - Mammon (5층): 2페이즈
   - Eligor (10층): 2페이즈
   - Lucifuge (15층): 3페이즈

2. **페이즈별 패턴 (Phase Patterns)**
   - Phase 1: 기본 패턴
   - Phase 2: 강화된 패턴
   - Phase 3: 최종 형태 (Lucifuge만)

3. **AI 척도 (AI Intelligence Level)**
   ```javascript
   AI_LEVEL = {
     BASIC: 0,      // 1-3층: 랜덤 주사위, 스킬 없음
     STANDARD: 1,   // 4-9층: 기본 전략, 스킬 1-2개
     ADVANCED: 2,   // 10-14층: 족보 예측, 스킬 2-3개
     MASTER: 3      // 15층: 최적 플레이, 스킬 4개
   }
   ```

4. **보스 제한 능력 (Boss Restrictions)**
   - Mammon: 특정 족보 봉인, 리스크/리워드 메커니즘
   - Eligor: 받는 데미지 감소, 카운터 어택
   - Lucifuge: 6-9개 주사위, 차원 주사위 사용

**에이전트 개발 지침**:
```javascript
// ❌ 잘못된 구현 (HP로 난이도 조절)
const boss = {
  hp: 300,  // 잘못됨!
};

// ✅ 올바른 구현 (페이즈 시스템)
const boss = {
  id: 'lucifuge',
  currentPhase: 1,
  maxPhases: 3,
  hp: 100,  // 현재 페이즈 HP
  phases: [
    {
      phase: 1,
      hp: 100,
      aiLevel: 2,
      pattern: 'testing',
      skills: ['skill1', 'skill2']
    },
    {
      phase: 2,
      hp: 100,
      aiLevel: 3,
      pattern: 'serious',
      skills: ['skill3', 'skill4']
    },
    {
      phase: 3,
      hp: 100,
      aiLevel: 3,
      pattern: 'true_form',
      skills: ['skill5', 'skill6', 'skill7', 'skill8'],
      specialAbility: 'dimensional_dice'
    }
  ]
};

// 페이즈 전환 로직
function checkPhaseTransition(boss) {
  if (boss.hp <= 0 && boss.currentPhase < boss.maxPhases) {
    boss.currentPhase++;
    boss.hp = 100;  // HP 리셋
    
    // 페이즈 전환 애니메이션
    playPhaseTransitionCutscene(boss.currentPhase);
    
    // 새로운 페이즈 패턴 적용
    applyPhasePattern(boss, boss.currentPhase);
  } else if (boss.hp <= 0 && boss.currentPhase === boss.maxPhases) {
    // 보스 격파
    onBossDefeated(boss.id);
  }
}
```

**절대 금지**:
- ❌ 단일 HP를 100 이상으로 설정
- ❌ 페이즈당 HP를 100 이외의 값으로 설정
- ❌ "HP 증가" 스킬 추가
- ❌ "최대 HP 증가" 업그레이드
- ❌ 페이즈 없이 HP만 늘려서 난이도 조절

#### 2.2.3 스킬 밸런스 철학
- **희귀도 ≠ 강함**: Epic이 Legendary보다 상황에 따라 더 유용할 수 있음
- **시너지 중시**: 4개 스킬의 조합이 개별 스킬보다 중요
- **카운터 존재**: 모든 빌드에는 약점이 있어야 함

---

## 3. 게임 시스템 규칙

### 3.1 스킬 시스템

#### 3.1.1 스킬 구조 템플릿
```javascript
{
  id: "unique_skill_id",
  name: "Skill Name (영문)",
  nameKR: "스킬 이름 (한글)",
  rarity: "Common" | "Rare" | "Epic" | "Legendary",
  description: "간결한 효과 설명 (1-2문장)",
  effect: function(gameState) { /* 구현 */ },
  trigger: "BATTLE_START" | "DICE_ROLL" | "DAMAGE_DEALT" | "DAMAGE_TAKEN" | "PASSIVE",
  icon: "path/to/icon.png",
  unlockCondition: "기본 제공" | "5층 보스 클리어" 등
}
```

#### 3.1.2 스킬 작성 규칙
1. **명확성**: 효과는 모호함 없이 정확히 기술
2. **일관성**: 유사 스킬은 동일 구조/용어 사용
3. **투명성**: 숨겨진 효과 금지 (모든 효과는 UI에 표시)
4. **테스트 가능성**: 효과를 단위 테스트로 검증 가능해야 함

#### 3.1.3 스킬 예시 (참고용)
```javascript
// GOOD EXAMPLE
{
  name: "Lucky Reroll",
  rarity: "Common",
  description: "매 전투 시작 시 주사위 1개 자동 재굴림",
  effect: (state) => {
    if (state.phase === "BATTLE_START") {
      state.rerollDice(1);
    }
  }
}

// BAD EXAMPLE (모호함)
{
  name: "Fortune's Favor",
  description: "행운을 증가시킵니다", // ❌ "행운"이 무엇인지 불명확
  effect: (state) => {
    state.luck += 1; // ❌ "luck" 스탯이 시스템에 없음
  }
}
```

### 3.2 PvE 캠페인 구조

#### 3.2.1 15층 구조 (엄격히 준수)
```
1층: 일반 전투 3회
2층: 일반 전투 3회
3층: 일반 전투 4회
4층: 엘리트 1회
5층: 보스 #1 (Mammon) → Rare 스킬 1개 선택
6층: 일반 전투 3회
7층: 일반 전투 4회
8층: 일반 전투 3회
9층: 엘리트 1회
10층: 보스 #2 (Eligor) → Epic 스킬 1개 선택
11층: 일반 전투 4회
12층: 엘리트 1회
13층: 일반 전투 4회
14층: 엘리트 1회
15층: 최종 보스 (Lucifuge) → Legendary 스킬 1개 선택
```

**에이전트 지침**:
- 층 순서 변경 불가
- 보스 위치 변경 불가 (5층, 10층, 15층 고정)
- 보스 스킬 보상 등급 변경 불가

#### 3.2.2 전투 플로우 (턴제 시스템)

**⚠️ 중요: 턴제(Turn-based) 시스템입니다. 동시에 주사위를 굴리지 않습니다!**

```
[턴 1: 플레이어]
1. 전투 시작 → 플레이어 장착 스킬 자동 발동 (BATTLE_START 트리거)
2. 플레이어 주사위 굴림 (3개)
3. 플레이어 스킬 효과 적용 (DICE_ROLL 트리거)
4. 플레이어 족보 판정
5. 플레이어 공격력 계산
6. 적에게 데미지 처리 (DAMAGE_DEALT 트리거)
7. 적 HP 감소 애니메이션

[턴 2: 적/보스]
8. 적 차례 시작 → 적 AI 스킬 발동
9. 적 주사위 굴림 (3개)
10. 적 스킬 효과 적용
11. 적 족보 판정
12. 적 공격력 계산
13. 플레이어에게 데미지 처리
14. 플레이어 HP 감소 애니메이션

[턴 종료]
15. 양측 HP 체크 (0 이하면 전투 종료)
16. 최대 턴 체크 (10턴 초과 시 무승부)
17. 다음 턴으로 (턴 3: 플레이어 → 턴 4: 적 → ...)
```

**에이전트 개발 지침**:
```javascript
// ❌ 잘못된 구현 (동시 실행)
async function battle() {
  const playerRoll = rollDice();
  const enemyRoll = rollDice(); // 동시에 굴림 - 잘못됨!
  // ...
}

// ✅ 올바른 구현 (턴제)
async function battle() {
  let turn = 1;
  let currentTurn = 'player';
  
  while (player.hp > 0 && enemy.hp > 0 && turn <= 10) {
    if (currentTurn === 'player') {
      await executePlayerTurn();
      currentTurn = 'enemy';
    } else {
      await executeEnemyTurn();
      currentTurn = 'player';
      turn++;
    }
  }
}
```

### 3.3 PvP 랭크 모드

#### 3.3.1 매칭 시스템
- **ELO 기반**: ±150 범위 내 매칭
- **대기 시간**: 30초 초과 시 범위 확대 (±50씩 증가)
- **최대 대기**: 3분 (범위 무제한)

#### 3.3.2 턴 시간 제한
- **플레이어 턴**: 30초 제한
- **타임아웃**: 자동으로 주사위 굴림
- **경고**: 10초 남았을 때 UI 경고

#### 3.3.3 보상 체계
```
승리: ELO +25, 영혼석 20
패배: ELO -25, 영혼석 5
무승부: ELO ±0, 영혼석 10
```

**에이전트 지침**:
- ELO 계산식 변경 금지
- 영혼석은 코스메틱 상점 전용 화폐
- 영혼석으로 스킬 구매 절대 불가

---

## 3. 세계관 및 UI 표준 (간소화)

### 3.1 세계관 핵심 규칙
- **시대**: 1920년대 아르데코 양식 (절대 벗어나지 말 것)
- **금지 요소**: 스마트폰, 현대 건축, 네온사인, 전자기기
- **허용 요소**: 턱시도, 샹들리에, 대리석, 금장식, 재즈 음악

### 3.2 캐릭터 대사 규칙
```javascript
// Lucifuge 대사 (최종 보스)
const LUCIFUGE_QUOTES = [
  "The dice have spoken.",
  "Fate is absolute.",
  "You defy probability itself."
];

// ✅ 짧고 철학적, 감정 억제
// ❌ "하하하! 감히 나에게!" 같은 과장 금지
```

### 3.3 크로스 플랫폼 UI/UX 규칙 (필수)

#### 3.3.1 브라우저 기본 팝업 절대 금지
**⚠️ 중요: alert(), confirm(), prompt() 사용 금지!**

크로스 플랫폼 지원을 위해 브라우저 기본 팝업은 사용하지 않습니다.

```javascript
// ❌ 절대 금지
alert('전투에서 승리했습니다!');
if (confirm('게임을 종료하시겠습니까?')) {
  exitGame();
}
const name = prompt('닉네임을 입력하세요');

// ✅ 커스텀 모달 컴포넌트 사용
showModal({
  type: 'info',
  title: '승리',
  message: '전투에서 승리했습니다!',
  buttons: [{ text: '확인', action: closeModal }]
});

showModal({
  type: 'confirm',
  title: '게임 종료',
  message: '게임을 종료하시겠습니까?',
  buttons: [
    { text: '취소', action: closeModal },
    { text: '종료', action: exitGame, variant: 'danger' }
  ]
});

showModal({
  type: 'input',
  title: '닉네임 설정',
  placeholder: '닉네임을 입력하세요',
  buttons: [
    { text: '취소', action: closeModal },
    { text: '확인', action: (value) => setNickname(value) }
  ]
});
```

**커스텀 모달 구현 예시 (Vue 3)**:
```vue
<!-- CustomModal.vue -->
<template>
  <Teleport to="body">
    <div v-if="isOpen" class="modal-overlay" @click="handleOverlayClick">
      <div class="modal-container" @click.stop>
        <h2 class="modal-title">{{ title }}</h2>
        <p class="modal-message">{{ message }}</p>
        
        <!-- Input 타입 -->
        <input 
          v-if="type === 'input'" 
          v-model="inputValue"
          :placeholder="placeholder"
          class="modal-input"
        />
        
        <!-- 버튼들 -->
        <div class="modal-buttons">
          <button
            v-for="btn in buttons"
            :key="btn.text"
            :class="['modal-btn', btn.variant]"
            @click="handleButtonClick(btn)"
          >
            {{ btn.text }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref } from 'vue';

const props = defineProps({
  isOpen: Boolean,
  type: String,
  title: String,
  message: String,
  placeholder: String,
  buttons: Array
});

const inputValue = ref('');

const handleButtonClick = (btn) => {
  if (props.type === 'input') {
    btn.action(inputValue.value);
  } else {
    btn.action();
  }
};
</script>
```

#### 3.3.2 필수 네비게이션 버튼 (모든 화면)
**⚠️ 모든 화면에 다음 버튼을 배치해야 합니다:**

```vue
<!-- AppNavigation.vue -->
<template>
  <div class="app-navigation">
    <!-- 홈 버튼 (좌측 상단) -->
    <button 
      class="nav-btn home-btn"
      @click="goToHome"
      aria-label="홈으로"
    >
      <HomeIcon />
    </button>
    
    <!-- 뒤로가기 버튼 (홈 버튼 옆) -->
    <button 
      v-if="canGoBack"
      class="nav-btn back-btn"
      @click="goBack"
      aria-label="뒤로가기"
    >
      <BackIcon />
    </button>
    
    <!-- 설정 버튼 (우측 상단) -->
    <button 
      class="nav-btn settings-btn"
      @click="openSettings"
      aria-label="설정"
    >
      <SettingsIcon />
    </button>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const canGoBack = computed(() => {
  return window.history.length > 1;
});

const goToHome = () => {
  router.push('/');
};

const goBack = () => {
  router.back();
};

const openSettings = () => {
  router.push('/settings');
};
</script>

<style scoped>
.app-navigation {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
}

.nav-btn {
  width: 44px;
  height: 44px;
  border: none;
  background: transparent;
  cursor: pointer;
  transition: transform 0.2s;
}

.nav-btn:hover {
  transform: scale(1.1);
}

.nav-btn:active {
  transform: scale(0.95);
}
</style>
```

#### 3.3.3 설정 화면 필수 옵션
**⚠️ 설정 화면에는 다음 옵션이 반드시 포함되어야 합니다:**

```vue
<!-- SettingsScreen.vue -->
<template>
  <div class="settings-screen">
    <h1>설정</h1>
    
    <!-- 사운드 설정 -->
    <section class="settings-section">
      <h2>사운드</h2>
      
      <!-- BGM 볼륨 -->
      <div class="setting-item">
        <label>BGM 볼륨</label>
        <input 
          type="range" 
          v-model="settings.bgmVolume"
          min="0" 
          max="100"
          @input="updateBGMVolume"
        />
        <span>{{ settings.bgmVolume }}%</span>
      </div>
      
      <!-- 효과음 볼륨 -->
      <div class="setting-item">
        <label>효과음 볼륨</label>
        <input 
          type="range" 
          v-model="settings.sfxVolume"
          min="0" 
          max="100"
          @input="updateSFXVolume"
        />
        <span>{{ settings.sfxVolume }}%</span>
      </div>
      
      <!-- 음소거 -->
      <div class="setting-item">
        <label>음소거</label>
        <button 
          class="toggle-btn"
          :class="{ active: settings.muted }"
          @click="toggleMute"
        >
          {{ settings.muted ? 'ON' : 'OFF' }}
        </button>
      </div>
    </section>
    
    <!-- 진동 설정 (모바일만) -->
    <section v-if="isMobile" class="settings-section">
      <h2>진동</h2>
      
      <div class="setting-item">
        <label>진동</label>
        <button 
          class="toggle-btn"
          :class="{ active: settings.vibration }"
          @click="toggleVibration"
        >
          {{ settings.vibration ? 'ON' : 'OFF' }}
        </button>
      </div>
    </section>
    
    <!-- 그래픽 설정 -->
    <section class="settings-section">
      <h2>그래픽</h2>
      
      <div class="setting-item">
        <label>화질</label>
        <select v-model="settings.quality" @change="updateQuality">
          <option value="low">낮음</option>
          <option value="medium">중간</option>
          <option value="high">높음</option>
        </select>
      </div>
      
      <div class="setting-item">
        <label>애니메이션</label>
        <button 
          class="toggle-btn"
          :class="{ active: settings.animations }"
          @click="toggleAnimations"
        >
          {{ settings.animations ? 'ON' : 'OFF' }}
        </button>
      </div>
    </section>
    
    <!-- 게임플레이 설정 -->
    <section class="settings-section">
      <h2>게임플레이</h2>
      
      <div class="setting-item">
        <label>애니메이션 속도</label>
        <select v-model="settings.animationSpeed" @change="updateAnimationSpeed">
          <option value="0.5">느림 (2x 시간)</option>
          <option value="1">보통</option>
          <option value="1.5">빠름 (0.67x 시간)</option>
          <option value="2">매우 빠름 (0.5x 시간)</option>
        </select>
      </div>
      
      <div class="setting-item">
        <label>전투 스킵</label>
        <button 
          class="toggle-btn"
          :class="{ active: settings.battleSkip }"
          @click="toggleBattleSkip"
        >
          {{ settings.battleSkip ? 'ON' : 'OFF' }}
        </button>
      </div>
    </section>
    
    <!-- 저장 버튼 -->
    <button class="save-btn" @click="saveSettings">
      설정 저장
    </button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useSettingsStore } from '@/stores/settings';

const settingsStore = useSettingsStore();
const settings = ref({ ...settingsStore.settings });

const isMobile = computed(() => {
  return /Android|iPhone|iPad|iPod/i.test(navigator.userAgent);
});

// 사운드 관련
const updateBGMVolume = () => {
  settingsStore.setBGMVolume(settings.value.bgmVolume);
};

const updateSFXVolume = () => {
  settingsStore.setSFXVolume(settings.value.sfxVolume);
};

const toggleMute = () => {
  settings.value.muted = !settings.value.muted;
  settingsStore.setMuted(settings.value.muted);
};

// 진동 관련 (모바일만)
const toggleVibration = () => {
  settings.value.vibration = !settings.value.vibration;
  settingsStore.setVibration(settings.value.vibration);
  
  // 테스트 진동
  if (settings.value.vibration && navigator.vibrate) {
    navigator.vibrate(50);
  }
};

// 설정 저장
const saveSettings = () => {
  settingsStore.saveSettings(settings.value);
  
  // 저장 완료 모달 (alert 대신)
  showModal({
    type: 'info',
    title: '저장 완료',
    message: '설정이 저장되었습니다.',
    buttons: [{ text: '확인', action: closeModal }]
  });
};
</script>
```

#### 3.3.4 네비게이션 버튼 배치 규칙

```css
/* 모든 화면에 적용되는 네비게이션 바 */
.app-navigation {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  z-index: 1000;
  background: rgba(27, 27, 39, 0.95);
  backdrop-filter: blur(10px);
}

/* PC/태블릿 (가로 모드) */
@media (min-width: 768px) {
  .app-navigation {
    padding: 0 32px;
  }
  
  .nav-btn {
    width: 48px;
    height: 48px;
  }
}

/* 모바일 (세로 모드) */
@media (max-width: 767px) {
  .app-navigation {
    padding: 0 16px;
  }
  
  .nav-btn {
    width: 44px;  /* 터치 최소 크기 */
    height: 44px;
  }
}

/* 컨텐츠 영역은 네비게이션 바 높이만큼 여백 */
.main-content {
  margin-top: 60px;
  padding-bottom: 80px; /* 하단 여백 (모바일 제스처 고려) */
}
```

#### 3.3.5 키보드 단축키 지원 (PC)

```javascript
// keyboardShortcuts.js
export function setupKeyboardShortcuts() {
  window.addEventListener('keydown', (e) => {
    // ESC: 설정 열기/닫기
    if (e.key === 'Escape') {
      toggleSettings();
    }
    
    // H: 홈으로
    if (e.key === 'h' || e.key === 'H') {
      if (!isTyping()) {
        goToHome();
      }
    }
    
    // Backspace: 뒤로가기
    if (e.key === 'Backspace') {
      if (!isTyping()) {
        e.preventDefault();
        goBack();
      }
    }
    
    // M: 음소거 토글
    if (e.key === 'm' || e.key === 'M') {
      if (!isTyping()) {
        toggleMute();
      }
    }
    
    // Space: 전투 스킵
    if (e.key === ' ') {
      if (isBattleActive() && !isTyping()) {
        e.preventDefault();
        skipBattleAnimation();
      }
    }
  });
}

function isTyping() {
  const activeElement = document.activeElement;
  return activeElement.tagName === 'INPUT' || 
         activeElement.tagName === 'TEXTAREA' ||
         activeElement.isContentEditable;
}
```

#### 3.3.6 모바일 제스처 지원

```javascript
// gestureSupport.js
export function setupGestureSupport() {
  let touchStartX = 0;
  let touchStartY = 0;
  
  document.addEventListener('touchstart', (e) => {
    touchStartX = e.touches[0].clientX;
    touchStartY = e.touches[0].clientY;
  }, { passive: true });
  
  document.addEventListener('touchend', (e) => {
    const touchEndX = e.changedTouches[0].clientX;
    const touchEndY = e.changedTouches[0].clientY;
    
    const deltaX = touchEndX - touchStartX;
    const deltaY = touchEndY - touchStartY;
    
    // 가로 스와이프 (최소 100px)
    if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > 100) {
      if (deltaX > 0) {
        // 오른쪽 스와이프: 뒤로가기
        if (canGoBack()) {
          goBack();
        }
      }
    }
    
    // 세로 스와이프 (최소 150px)
    if (Math.abs(deltaY) > Math.abs(deltaX) && Math.abs(deltaY) > 150) {
      if (deltaY < 0) {
        // 위로 스와이프: 새로고침
        refreshCurrentPage();
      }
    }
  }, { passive: true });
}
```

### 3.4 UI 컬러 (층별 변화)
```javascript
// 1-5층 (아르데코)
const FLOOR_1_5_COLORS = {
  primary: '#D4AF37',    // 금색
  secondary: '#FFFDD0',  // 크림색
};

// 6-10층 (균열)
const FLOOR_6_10_COLORS = {
  primary: '#808080',    // 회색
  secondary: '#6A0DAD',  // 보라색
};

// 11-15층 (코즈믹 호러)
const FLOOR_11_15_COLORS = {
  primary: '#6A0DAD',    // 보라색
  secondary: '#000000',  // 검은색
  accent: '#FF10F0',     // 네온 핑크
};
```

---

## 4. 기술 스택 및 코딩 표준

### 4.1 필수 기술 스택
```javascript
// Frontend
- Vue 3 (Composition API)
- Three.js (주사위 물리)
- Pinia (상태 관리)
- Vite (빌드)
- STOMP.js (WebSocket 클라이언트)
- SockJS (WebSocket fallback)

// Backend
- Spring Boot 3.2+
- Spring Data JPA
- Spring HATEOAS (RESTful API)
- Spring WebSocket (STOMP)
- MariaDB 11.x (플레이어 데이터)
- Redis (세션, 캐시, Pub/Sub)

// Infrastructure
- GCP (Cloud Storage, Cloud CDN, Compute Engine)
- WebSocket over STOMP (실시간 PvP)
```

### 4.2 API 설계 원칙 (RESTful + HATEOAS)

#### 4.2.1 HATEOAS 구조
**필수**: 모든 API 응답은 HATEOAS 링크를 포함해야 합니다.

```java
// ✅ 올바른 HATEOAS 응답 (Spring Boot)
@RestController
@RequestMapping("/api/v1")
public class BattleController {
    
    @GetMapping("/battles/{battleId}")
    public EntityModel<BattleDto> getBattle(@PathVariable Long battleId) {
        BattleDto battle = battleService.getBattle(battleId);
        
        // HATEOAS 링크 추가
        EntityModel<BattleDto> resource = EntityModel.of(battle);
        
        // self 링크
        resource.add(linkTo(methodOn(BattleController.class)
            .getBattle(battleId)).withSelfRel());
        
        // 가능한 액션 링크
        if (battle.getCurrentTurn().equals("player")) {
            resource.add(linkTo(methodOn(BattleController.class)
                .rollDice(battleId)).withRel("roll-dice"));
        }
        
        if (battle.getStatus().equals("ONGOING")) {
            resource.add(linkTo(methodOn(BattleController.class)
                .surrender(battleId)).withRel("surrender"));
        }
        
        // 관련 리소스 링크
        resource.add(linkTo(methodOn(PlayerController.class)
            .getPlayer(battle.getPlayerId())).withRel("player"));
        
        return resource;
    }
}
```

**응답 예시**:
```json
{
  "battleId": 12345,
  "playerId": 67890,
  "enemyId": 54321,
  "currentTurn": "player",
  "playerHp": 75,
  "enemyHp": 50,
  "turnCount": 3,
  "status": "ONGOING",
  "_links": {
    "self": {
      "href": "http://localhost:8080/api/v1/battles/12345"
    },
    "roll-dice": {
      "href": "http://localhost:8080/api/v1/battles/12345/roll"
    },
    "surrender": {
      "href": "http://localhost:8080/api/v1/battles/12345/surrender"
    },
    "player": {
      "href": "http://localhost:8080/api/v1/players/67890"
    }
  }
}
```

#### 4.2.2 RESTful API 엔드포인트 규칙

```java
// ✅ RESTful 리소스 구조
@RestController
@RequestMapping("/api/v1")
public class GameApiController {
    
    // 플레이어 리소스
    @GetMapping("/players/{playerId}")
    public EntityModel<PlayerDto> getPlayer(@PathVariable Long playerId) { }
    
    @PutMapping("/players/{playerId}")
    public EntityModel<PlayerDto> updatePlayer(@PathVariable Long playerId, @RequestBody PlayerUpdateDto dto) { }
    
    // 스킬 리소스
    @GetMapping("/players/{playerId}/skills")
    public CollectionModel<SkillDto> getPlayerSkills(@PathVariable Long playerId) { }
    
    @PostMapping("/players/{playerId}/skills/{skillId}/equip")
    public EntityModel<PlayerDto> equipSkill(@PathVariable Long playerId, @PathVariable Long skillId, @RequestParam int slotIndex) { }
    
    // 전투 리소스
    @PostMapping("/battles")
    public EntityModel<BattleDto> createBattle(@RequestBody BattleCreateDto dto) { }
    
    @PostMapping("/battles/{battleId}/roll")
    public EntityModel<BattleActionResultDto> rollDice(@PathVariable Long battleId) { }
    
    @PostMapping("/battles/{battleId}/surrender")
    public EntityModel<BattleDto> surrender(@PathVariable Long battleId) { }
    
    // 캠페인 리소스
    @GetMapping("/campaigns/{playerId}")
    public EntityModel<CampaignProgressDto> getCampaignProgress(@PathVariable Long playerId) { }
    
    @PostMapping("/campaigns/{playerId}/floors/{floor}/start")
    public EntityModel<BattleDto> startFloor(@PathVariable Long playerId, @PathVariable int floor) { }
}
```

**에이전트 지침**:
- HTTP 메서드를 의미에 맞게 사용 (GET, POST, PUT, DELETE)
- URI는 명사 중심 (동사 사용 금지)
- 계층 구조를 명확히 표현 (`/players/{id}/skills`)
- 모든 응답에 `_links` 포함

### 4.3 WebSocket (STOMP) 구성

#### 4.3.1 Spring Boot WebSocket 설정

```java
// ✅ WebSocket STOMP 설정
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트로 메시지 전송할 때 사용할 prefix
        config.enableSimpleBroker("/topic", "/queue");
        
        // 클라이언트에서 서버로 메시지 보낼 때 사용할 prefix
        config.setApplicationDestinationPrefixes("/app");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS();  // SockJS fallback 지원
    }
}
```

#### 4.3.2 실시간 PvP 메시지 처리

```java
// ✅ PvP 실시간 메시지 컨트롤러
@Controller
public class PvPWebSocketController {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final BattleService battleService;
    
    // 클라이언트 → 서버: 주사위 굴림
    @MessageMapping("/pvp/battles/{battleId}/roll")
    @SendToUser("/queue/battle-result")
    public BattleActionResultDto rollDice(
        @DestinationVariable Long battleId,
        @Payload RollDiceDto dto,
        @Header("simpSessionId") String sessionId
    ) {
        // 서버에서 주사위 생성
        int[] dice = battleService.rollDice(battleId, dto.getPlayerId());
        
        // 족보 판정
        HandResult hand = battleService.evaluateHand(dice);
        
        // 데미지 계산
        int damage = battleService.calculateDamage(hand, dto.getPlayerId());
        
        // 상대방에게도 전송
        messagingTemplate.convertAndSend(
            "/topic/pvp/battles/" + battleId,
            new BattleUpdateDto(battleId, dice, hand, damage)
        );
        
        return new BattleActionResultDto(dice, hand, damage);
    }
    
    // 매칭 대기열
    @MessageMapping("/pvp/matchmaking/join")
    @SendToUser("/queue/match-found")
    public void joinMatchmaking(@Payload MatchmakingDto dto) {
        matchmakingService.addToQueue(dto.getPlayerId(), dto.getElo());
    }
    
    // 턴 종료 알림
    @MessageMapping("/pvp/battles/{battleId}/end-turn")
    public void endTurn(@DestinationVariable Long battleId, @Payload EndTurnDto dto) {
        battleService.endTurn(battleId, dto.getPlayerId());
        
        // 상대방에게 턴 시작 알림
        messagingTemplate.convertAndSend(
            "/topic/pvp/battles/" + battleId + "/turn-change",
            new TurnChangeDto(battleId, "opponent")
        );
    }
}
```

#### 4.3.3 Vue 3 WebSocket 클라이언트

```javascript
// ✅ Vue 3 Composable: useWebSocket.js
import { ref, onMounted, onUnmounted } from 'vue';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export function useWebSocket() {
  const stompClient = ref(null);
  const connected = ref(false);
  
  const connect = () => {
    const socket = new SockJS('http://localhost:8080/ws');
    stompClient.value = new Client({
      webSocketFactory: () => socket,
      debug: (str) => {
        console.log('STOMP:', str);
      },
      onConnect: (frame) => {
        connected.value = true;
        console.log('Connected:', frame);
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
      }
    });
    
    stompClient.value.activate();
  };
  
  const disconnect = () => {
    if (stompClient.value) {
      stompClient.value.deactivate();
      connected.value = false;
    }
  };
  
  const subscribe = (destination, callback) => {
    if (stompClient.value && connected.value) {
      return stompClient.value.subscribe(destination, callback);
    }
  };
  
  const send = (destination, body) => {
    if (stompClient.value && connected.value) {
      stompClient.value.publish({
        destination,
        body: JSON.stringify(body)
      });
    }
  };
  
  onMounted(() => {
    connect();
  });
  
  onUnmounted(() => {
    disconnect();
  });
  
  return {
    connected,
    subscribe,
    send,
    disconnect
  };
}
```

**사용 예시**:
```vue
<script setup>
import { ref, onMounted } from 'vue';
import { useWebSocket } from '@/composables/useWebSocket';

const battleId = ref(12345);
const battleUpdate = ref(null);

const { connected, subscribe, send } = useWebSocket();

onMounted(() => {
  // 전투 업데이트 구독
  subscribe(`/topic/pvp/battles/${battleId.value}`, (message) => {
    battleUpdate.value = JSON.parse(message.body);
    console.log('Battle update:', battleUpdate.value);
  });
  
  // 턴 변경 구독
  subscribe(`/topic/pvp/battles/${battleId.value}/turn-change`, (message) => {
    const turnChange = JSON.parse(message.body);
    console.log('Turn changed to:', turnChange.currentTurn);
  });
});

// 주사위 굴리기
const rollDice = () => {
  send(`/app/pvp/battles/${battleId.value}/roll`, {
    playerId: 67890
  });
};
</script>
```

### 4.4 JPA + MariaDB 설정

#### 4.4.1 application.yml 설정

```yaml
spring:
  datasource:
    driver-class-name: org.mariadb.jdbc.Driver
    url: jdbc:mariadb://localhost:3306/hotelsortis?useUnicode=true&characterEncoding=utf8mb4
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
  
  jpa:
    hibernate:
      ddl-auto: validate  # 프로덕션에서는 validate 사용
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MariaDBDialect
        format_sql: true
        use_sql_comments: true
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
  
  redis:
    host: localhost
    port: 6379
    password: ${REDIS_PASSWORD}
    lettuce:
      pool:
        max-active: 10
        max-idle: 5
```

#### 4.4.2 JPA 엔티티 예시

```java
// ✅ Player 엔티티
@Entity
@Table(name = "players", indexes = {
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_elo", columnList = "elo")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Player extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 20)
    private String username;
    
    @Column(nullable = false)
    private Integer elo = 1000;
    
    @Column(nullable = false)
    private Integer soulStones = 0;  // 영혼석 (코스메틱 화폐)
    
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    private List<PlayerSkill> skills = new ArrayList<>();
    
    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL)
    private CampaignProgress campaignProgress;
    
    @Column(nullable = false)
    private Integer currentFloor = 1;
    
    @Column(nullable = false)
    private Integer highestFloorCleared = 0;
    
    // 스킬 장착 (4개 고정)
    @Column(length = 1000)
    private String equippedSkillIds;  // JSON: "[1,2,3,4]"
    
    public void equipSkill(Long skillId, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= 4) {
            throw new IllegalArgumentException("Slot index must be 0-3");
        }
        // 로직...
    }
}

// ✅ Battle 엔티티
@Entity
@Table(name = "battles", indexes = {
    @Index(name = "idx_player_id", columnList = "player_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Battle extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;
    
    @Column(nullable = false)
    private Long enemyId;  // AI 또는 상대 플레이어 ID
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BattleType type;  // PVE, PVP
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BattleStatus status = BattleStatus.ONGOING;
    
    @Column(nullable = false)
    private Integer playerHp = 100;
    
    @Column(nullable = false)
    private Integer enemyHp = 100;
    
    @Column(nullable = false)
    private Integer turnCount = 1;
    
    @Column(nullable = false)
    private String currentTurn = "player";  // "player" or "enemy"
    
    @OneToMany(mappedBy = "battle", cascade = CascadeType.ALL)
    private List<BattleLog> logs = new ArrayList<>();
    
    // 턴 진행
    public void executeTurn(int[] dice, HandResult hand, int damage) {
        if (currentTurn.equals("player")) {
            enemyHp -= damage;
            currentTurn = "enemy";
        } else {
            playerHp -= damage;
            currentTurn = "player";
            turnCount++;
        }
        
        // 승패 체크
        if (playerHp <= 0) {
            status = BattleStatus.DEFEAT;
        } else if (enemyHp <= 0) {
            status = BattleStatus.VICTORY;
        } else if (turnCount > 10) {
            status = BattleStatus.DRAW;
        }
    }
}

// ✅ BaseEntity (공통 필드)
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class BaseEntity {
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

#### 4.4.3 JPA Repository 예시

```java
// ✅ PlayerRepository
@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    
    Optional<Player> findByUsername(String username);
    
    @Query("SELECT p FROM Player p WHERE p.elo BETWEEN :minElo AND :maxElo AND p.id != :excludeId")
    List<Player> findMatchmakingCandidates(
        @Param("minElo") int minElo,
        @Param("maxElo") int maxElo,
        @Param("excludeId") Long excludeId
    );
    
    @Query("SELECT p FROM Player p JOIN FETCH p.skills WHERE p.id = :id")
    Optional<Player> findByIdWithSkills(@Param("id") Long id);
}

// ✅ BattleRepository
@Repository
public interface BattleRepository extends JpaRepository<Battle, Long> {
    
    @Query("SELECT b FROM Battle b WHERE b.player.id = :playerId AND b.status = :status")
    Optional<Battle> findOngoingBattle(
        @Param("playerId") Long playerId,
        @Param("status") BattleStatus status
    );
    
    @Query("SELECT b FROM Battle b JOIN FETCH b.logs WHERE b.id = :id")
    Optional<Battle> findByIdWithLogs(@Param("id") Long id);
}
```

---

### 4.5 코딩 표준

#### 4.5.1 네이밍 컨벤션

```java
// Java/Spring Boot
// 클래스: PascalCase
public class BattleService { }
public class PlayerDto { }

// 메서드/변수: camelCase
private int playerHealth = 100;
public void rollDice() { }

// 상수: UPPER_SNAKE_CASE
public static final int MAX_SKILL_SLOTS = 4;
public static final int DICE_COUNT = 3;

// 패키지: lowercase
package com.hotelsortis.game.battle;
package com.hotelsortis.game.skill;
```

```javascript
// Vue 3 / JavaScript
// 컴포넌트: PascalCase
BattleScreen.vue
SkillLoadout.vue

// 함수/변수: camelCase
const playerHealth = 100;
function rollDice() { }

// 상수: UPPER_SNAKE_CASE
const MAX_SKILL_SLOTS = 4;

// 파일명: kebab-case (composables, utils)
use-websocket.js
battle-utils.js
```

#### 4.5.2 에러 처리 (필수)

```java
// ✅ Spring Boot 구체적인 에러 처리
@Service
@RequiredArgsConstructor
public class BattleService {
    
    private final BattleRepository battleRepository;
    
    @Transactional
    public BattleActionResultDto rollDice(Long battleId, Long playerId) {
        // 1. 전투 존재 확인
        Battle battle = battleRepository.findById(battleId)
            .orElseThrow(() -> new BattleNotFoundException(
                "Battle not found: " + battleId
            ));
        
        // 2. 턴 검증
        if (!battle.isPlayerTurn()) {
            throw new InvalidTurnException(
                "Not player's turn in battle: " + battleId
            );
        }
        
        // 3. 주사위 굴림 (서버에서!)
        int[] dice = diceService.roll();
        
        // 4. 로그 기록
        log.info("Player {} rolled dice in battle {}: {}", 
            playerId, battleId, Arrays.toString(dice));
        
        try {
            // 5. 족보 판정 및 처리
            HandResult hand = handEvaluator.evaluate(dice);
            int damage = damageCalculator.calculate(hand, playerId);
            
            battle.executeTurn(dice, hand, damage);
            battleRepository.save(battle);
            
            return BattleActionResultDto.of(dice, hand, damage);
            
        } catch (Exception e) {
            log.error("Error processing dice roll for battle {}", battleId, e);
            throw new BattleProcessingException(
                "Failed to process dice roll", e
            );
        }
    }
}

// ❌ 모호한 에러 처리
public void rollDice(Long battleId) {
    try {
        // ...
    } catch (Exception e) {
        System.out.println("Error");  // 금지!
    }
}
```

```javascript
// ✅ Vue 3 구체적인 에러 처리
async function rollDice() {
  try {
    const response = await api.post(`/battles/${battleId.value}/roll`);
    
    if (!response.data) {
      throw new Error(`Roll failed for battle ${battleId.value}`);
    }
    
    battleResult.value = response.data;
    
  } catch (error) {
    console.error('Dice roll error:', {
      battleId: battleId.value,
      error: error.message
    });
    
    // 사용자에게 표시 (alert 금지!)
    showModal({
      type: 'error',
      title: '오류',
      message: '주사위 굴림에 실패했습니다.',
      buttons: [{ text: '확인', action: closeModal }]
    });
    
    throw new DiceRollError(error.message);
  }
}
```

#### 4.5.3 트랜잭션 관리 (필수)

```java
// ✅ 트랜잭션 범위 명확히
@Service
@RequiredArgsConstructor
public class CampaignService {
    
    private final PlayerRepository playerRepository;
    private final BattleRepository battleRepository;
    private final SkillRepository skillRepository;
    
    // 읽기 전용 트랜잭션
    @Transactional(readOnly = true)
    public CampaignProgressDto getProgress(Long playerId) {
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new PlayerNotFoundException(playerId));
        
        return CampaignProgressDto.from(player);
    }
    
    // 쓰기 트랜잭션
    @Transactional
    public BattleDto startFloor(Long playerId, int floor) {
        // 1. 플레이어 검증
        Player player = playerRepository.findByIdWithSkills(playerId)
            .orElseThrow(() -> new PlayerNotFoundException(playerId));
        
        // 2. 층 검증
        if (floor > player.getHighestFloorCleared() + 1) {
            throw new InvalidFloorException(
                "Cannot skip floors. Current: " + player.getHighestFloorCleared()
            );
        }
        
        // 3. 전투 생성
        Battle battle = Battle.createPvE(player, floor);
        battle = battleRepository.save(battle);
        
        // 4. 플레이어 상태 업데이트
        player.setCurrentFloor(floor);
        playerRepository.save(player);
        
        return BattleDto.from(battle);
    }
    
    // 보스 클리어 시 스킬 보상
    @Transactional
    public void onBossDefeated(Long playerId, int floor) {
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new PlayerNotFoundException(playerId));
        
        // 스킬 보상
        List<Skill> offeredSkills = skillRepository
            .findByRarityAndNotOwnedByPlayer(
                getSkillRarity(floor), 
                playerId,
                PageRequest.of(0, 3)
            );
        
        // ... 스킬 선택 로직
        
        // 진행도 업데이트
        player.updateHighestFloorCleared(floor);
        playerRepository.save(player);
    }
}
```

### 4.6 성능 목표 (필수 준수)

```java
// Spring Boot 성능 요구사항
public class PerformanceRequirements {
    public static final int INITIAL_LOAD_MS = 5000;      // 5초 이내
    public static final int API_RESPONSE_P99_MS = 100;   // 100ms 이내 (P99)
    public static final int BATTLE_START_MS = 500;       // 0.5초 이내
    public static final int DB_QUERY_MAX_MS = 50;        // 50ms 이내
    public static final int WEBSOCKET_LATENCY_MS = 50;   // 50ms 이내
}

// 성능 미달 시 배포 금지!
```

**JPA 쿼리 최적화 필수**:
```java
// ✅ N+1 문제 해결
@Query("SELECT p FROM Player p JOIN FETCH p.skills WHERE p.id = :id")
Optional<Player> findByIdWithSkills(@Param("id") Long id);

// ✅ 배치 처리
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true

// ✅ 인덱스 활용
@Table(indexes = {
    @Index(name = "idx_player_elo", columnList = "elo"),
    @Index(name = "idx_battle_status", columnList = "status")
})
```

### 4.7 보안 규칙 (필수)

#### 4.7.1 절대 금지 사항

```java
// ❌ 클라이언트에서 절대 하면 안 되는 것들

// 1. 주사위 결과 생성 (Vue)
function rollDice() {
  return [
    Math.floor(Math.random() * 6) + 1,
    Math.floor(Math.random() * 6) + 1,
    Math.floor(Math.random() * 6) + 1
  ];  // 금지! 서버에서만
}

// 2. 족보 판정 (Vue)
function evaluateHand(dice) {
  // 클라이언트에서 판정 금지!
}

// 3. 데미지 계산 (Vue)
function calculateDamage(hand) {
  return hand.power;  // 금지! 서버에서만
}
```

#### 4.7.2 서버 검증 (필수)

```java
// ✅ 모든 게임 로직은 서버에서 검증
@PostMapping("/api/v1/battles/{battleId}/roll")
@PreAuthorize("isAuthenticated()")
public EntityModel<BattleActionResultDto> rollDice(
    @PathVariable Long battleId,
    @AuthenticationPrincipal UserPrincipal principal
) {
    // 1. 플레이어 검증
    Long playerId = principal.getId();
    
    // 2. 전투 소유권 검증
    battleService.validateBattleOwnership(battleId, playerId);
    
    // 3. 턴 검증
    battleService.validatePlayerTurn(battleId, playerId);
    
    // 4. 서버에서 주사위 굴림
    int[] dice = diceService.roll();
    
    // 5. 서버에서 족보 판정
    HandResult hand = handEvaluator.evaluate(dice);
    
    // 6. 서버에서 데미지 계산
    int damage = damageCalculator.calculate(hand, playerId);
    
    // 7. 전투 상태 업데이트
    Battle battle = battleService.executeTurn(battleId, dice, hand, damage);
    
    // 8. HATEOAS 링크와 함께 응답
    BattleActionResultDto result = BattleActionResultDto.of(dice, hand, damage);
    EntityModel<BattleActionResultDto> resource = EntityModel.of(result);
    
    // 다음 가능한 액션 링크
    if (battle.getStatus() == BattleStatus.ONGOING) {
        if (battle.getCurrentTurn().equals("enemy")) {
            // 적 턴이면 대기만 가능
            resource.add(linkTo(methodOn(BattleController.class)
                .getBattle(battleId)).withRel("wait"));
        }
    } else {
        // 전투 종료 시
        resource.add(linkTo(methodOn(BattleController.class)
            .getBattleResult(battleId)).withRel("result"));
    }
    
    return resource;
}
```

---

## 5. 테스트 및 품질 관리

### 5.1 필수 테스트 (배포 전)

#### 5.1.1 족보 판정 테스트 (100% 커버리지)
```javascript
// ✅ 모든 족보에 대한 테스트 필수 (Balanced System)
describe('evaluateHand', () => {
  // Ace: [1-1-1] → 45 DMG
  test('[1-1-1] = Ace (45)', () => {
    expect(evaluateHand([1, 1, 1])).toEqual({ rank: 'Ace', power: 45 });
  });

  // Triple: 동일 3개 [2-6] → 8 + (N*4)
  test('[2-2-2] = Triple (16)', () => {
    expect(evaluateHand([2, 2, 2])).toEqual({ rank: 'Triple', power: 16 });
  });
  test('[6-6-6] = Triple (32)', () => {
    expect(evaluateHand([6, 6, 6])).toEqual({ rank: 'Triple', power: 32 });
  });

  // Straight: [4-5-6] → 38 DMG
  test('[4-5-6] = Straight (38)', () => {
    expect(evaluateHand([4, 5, 6])).toEqual({ rank: 'Straight', power: 38 });
  });

  // Strike: [3-4-5] → 30 DMG
  test('[3-4-5] = Strike (30)', () => {
    expect(evaluateHand([3, 4, 5])).toEqual({ rank: 'Strike', power: 30 });
  });

  // Slash: [2-3-4] → 24 DMG
  test('[2-3-4] = Slash (24)', () => {
    expect(evaluateHand([2, 3, 4])).toEqual({ rank: 'Slash', power: 24 });
  });

  // Storm: [1-2-3] → 16 DMG
  test('[1-2-3] = Storm (16)', () => {
    expect(evaluateHand([1, 2, 3])).toEqual({ rank: 'Storm', power: 16 });
  });

  // Pair: 동일 2개 → 5 + (N*2)
  test('[3-3-5] = Pair (11)', () => {
    expect(evaluateHand([3, 3, 5])).toEqual({ rank: 'Pair', power: 11 });
  });

  // No Hand: 합계
  test('[1-3-5] = NoHand (9)', () => {
    expect(evaluateHand([1, 3, 5])).toEqual({ rank: 'NoHand', power: 9 });
  });
});
```

#### 5.1.2 전투 플로우 테스트
```javascript
// ✅ 턴제 시스템 검증
test('Battle flows in turns', async () => {
  const battle = new Battle(player, enemy);
  
  // 1. 플레이어가 먼저
  expect(battle.currentTurn).toBe('player');
  
  await battle.executeTurn();
  
  // 2. 적 차례로 전환
  expect(battle.currentTurn).toBe('enemy');
  
  await battle.executeTurn();
  
  // 3. 턴 카운트 증가
  expect(battle.turnCount).toBe(2);
});

// ✅ HP 100 검증
test('All characters start with 100 HP', () => {
  const player = createPlayer();
  const enemy = createEnemy(5);  // 5층 적
  const boss = createBoss('lucifuge');  // 15층 보스
  
  expect(player.hp).toBe(100);
  expect(enemy.hp).toBe(100);
  expect(boss.hp).toBe(100);  // 각 페이즈 HP
});

// ✅ 보스 페이즈 시스템 검증
test('Boss phase system works correctly', async () => {
  const boss = new BossPhaseManager('lucifuge');
  
  // Phase 1 시작
  expect(boss.currentPhase).toBe(1);
  expect(boss.hp).toBe(100);
  expect(boss.config.phases).toBe(3);
  
  // Phase 1 HP를 0으로
  boss.hp = 0;
  const transitioned = boss.checkPhaseTransition();
  
  expect(transitioned).toBe(true);
  expect(boss.currentPhase).toBe(2);
  expect(boss.hp).toBe(100);  // HP 리셋
  
  // Phase 2 HP를 0으로
  boss.hp = 0;
  boss.checkPhaseTransition();
  
  expect(boss.currentPhase).toBe(3);
  expect(boss.hp).toBe(100);  // HP 리셋
  
  // Phase 3 (마지막) HP를 0으로
  boss.hp = 0;
  const defeated = !boss.checkPhaseTransition();
  
  expect(defeated).toBe(true);
  expect(boss.currentPhase).toBe(3);
});

// ✅ 페이즈별 설정 변경 검증
test('Boss abilities change per phase', () => {
  const boss = new BossPhaseManager('mammon');
  
  // Phase 1
  const phase1Config = boss.config.phase1;
  expect(phase1Config.skills).toEqual(['greed_dice', 'double_or_nothing']);
  expect(phase1Config.aiLevel).toBe(AI_LEVELS.STANDARD);
  
  // Phase 2
  const phase2Config = boss.config.phase2;
  expect(phase2Config.skills.length).toBeGreaterThan(phase1Config.skills.length);
  expect(phase2Config.aiLevel).toBeGreaterThanOrEqual(phase1Config.aiLevel);
});
```

#### 5.1.3 스킬 슬롯 테스트
```javascript
// ✅ 최대 4개 슬롯 검증
test('Can equip 0 to 4 skills', () => {
  const loadout = new SkillLoadout();
  
  // 0개 장착 → 허용
  expect(loadout.validate()).toBe(true);
  expect(loadout.getEquippedCount()).toBe(0);
  
  // 1개 장착 → 허용
  loadout.equipSkill(skill1, 0);
  expect(loadout.validate()).toBe(true);
  expect(loadout.getEquippedCount()).toBe(1);
  
  // 4개 장착 → 허용
  loadout.equipSkill(skill2, 1);
  loadout.equipSkill(skill3, 2);
  loadout.equipSkill(skill4, 3);
  expect(loadout.validate()).toBe(true);
  expect(loadout.getEquippedCount()).toBe(4);
});

// ✅ 5개 이상 장착 시도 → 거부
test('Cannot equip more than 4 skills', () => {
  const loadout = new SkillLoadout();
  
  loadout.equipSkill(skill1, 0);
  loadout.equipSkill(skill2, 1);
  loadout.equipSkill(skill3, 2);
  loadout.equipSkill(skill4, 3);
  
  // 슬롯 가득 참 - 더 이상 장착 불가
  expect(() => loadout.equipSkill(skill5, 4)).toThrow('Invalid slot index');
});

// ✅ 중복 장착 방지
test('Cannot equip same skill twice', () => {
  const loadout = new SkillLoadout();
  
  loadout.equipSkill(skill1, 0);
  loadout.equipSkill(skill1, 1);  // 자동으로 슬롯 0에서 제거됨
  
  expect(loadout.getEquippedCount()).toBe(1);
  expect(loadout.slots[0]).toBe(null);
  expect(loadout.slots[1]).toBe(skill1);
});
```

### 5.2 버그 우선순위

```javascript
const BUG_PRIORITY = {
  CRITICAL: {
    // 24시간 내 핫픽스
    examples: [
      '게임 크래시',
      '데이터 손실',
      '무한 루프',
      '서버 다운'
    ]
  },
  HIGH: {
    // 3일 내 수정
    examples: [
      '전투 진행 불가',
      '스킬 미작동',
      '족보 오판정',
      'HP 계산 오류'
    ]
  },
  MEDIUM: {
    // 1주 내 수정
    examples: [
      '밸런스 이슈',
      'UI 버그',
      '애니메이션 끊김'
    ]
  },
  LOW: {
    // 백로그
    examples: [
      '텍스트 오타',
      '사소한 시각 버그'
    ]
  }
};
```

### 5.3 배포 전 체크리스트

```javascript
// ✅ 배포 전 필수 확인 사항
const DEPLOYMENT_CHECKLIST = [
  '[ ] 모든 단위 테스트 통과',
  '[ ] 족보 판정 100% 정확',
  '[ ] 턴제 시스템 정상 작동',
  '[ ] HP 모두 100으로 고정',
  '[ ] 스킬 슬롯 4개 검증',
  '[ ] Pay-to-Win 요소 없음',
  '[ ] 서버 검증 로직 적용',
  '[ ] 60 FPS 유지',
  '[ ] 로딩 시간 5초 이내',
  '[ ] 코드 리뷰 완료'
];
```

---

## 6. 빠른 참조 가이드

### 6.1 즉시 거부 체크리스트
개발 중 다음 요청이 들어오면 **즉시 거부하고 이유를 설명**하세요:

```javascript
// ❌ 절대 금지 목록
const FORBIDDEN_ACTIONS = {
  GAMEPLAY: [
    '족보 시스템 변경 (공격력, 순위, 조건)',
    '스킬 슬롯 4개 초과 설정 (최대 4개 고정)',
    'HP를 100 이외의 값으로 설정',
    '동시 주사위 굴림 (턴제 위반)',
    '주사위 결과 클라이언트 생성'
  ],
  MONETIZATION: [
    '스킬 구매 기능',
    '확률 조작 아이템',
    '스탯 부스터',
    '경험치 부스터',
    '부활 아이템',
    'PvP 어드밴티지 아이템'
  ],
  UI_UX: [
    'alert(), confirm(), prompt() 사용',
    '홈/뒤로가기/설정 버튼 없는 화면',
    '브라우저 기본 팝업 사용'
  ],
  WORLDBUILDING: [
    '1920년대 벗어난 요소 (스마트폰, 현대 건축)',
    '주인공 대사 추가',
    '보스 캐릭터 변경'
  ]
};
```

### 6.2 핵심 상수 (변경 금지)
```javascript
// 게임 시스템 상수
const GAME_CONSTANTS = {
  SKILL_SLOTS: 4,           // 스킬 슬롯
  DICE_COUNT: 3,            // 주사위 개수 (플레이어/일반 몬스터)
  HP: 100,                  // 모든 캐릭터 HP (페이즈당)
  MAX_TURNS: 10,            // 최대 턴 수
  TURN_TIME_LIMIT: 30,      // 턴당 시간 제한 (초)
  
  // 족보 공격력 (Balanced System)
  HAND_POWERS: {
    ACE: 45,                       // [1-1-1]
    TRIPLE: (n) => 8 + (n * 4),    // [2-6] → 16~32
    STRAIGHT: 38,                  // [4-5-6]
    STRIKE: 30,                    // [3-4-5]
    SLASH: 24,                     // [2-3-4]
    STORM: 16,                     // [1-2-3]
    PAIR: (n) => 5 + (n * 2),      // 7~17
    NO_HAND: (a,b,c) => a+b+c     // 3~16
  },
  
  // AI 레벨
  AI_LEVELS: {
    BASIC: 0,      // 1-3층
    STANDARD: 1,   // 4-9층
    ADVANCED: 2,   // 10-14층
    MASTER: 3      // 15층
  },
  
  // 보스 층 및 페이즈
  BOSS_CONFIG: {
    MAMMON: { floor: 5, phases: 2 },   // 총 HP 200
    ELIGOR: { floor: 10, phases: 2 },  // 총 HP 200
    LUCIFUGE: { floor: 15, phases: 3 } // 총 HP 300
  }
};
```

### 6.3 필수 검증 함수
```javascript
// 개발 시 사용할 검증 함수들
function validateGameState(state) {
  // HP 검증
  assert(state.player.hp <= 100, 'Player HP exceeds 100');
  assert(state.enemy.hp <= 100, 'Enemy HP exceeds 100');
  
  // 스킬 슬롯 검증
  assert(state.player.skills.length === 4, 'Must have exactly 4 skills');
  
  // 턴 검증
  assert(['player', 'enemy'].includes(state.currentTurn), 'Invalid turn');
  
  // 주사위 검증
  state.dice.forEach(d => {
    assert(d >= 1 && d <= 6, `Invalid dice value: ${d}`);
  });
}

function validateHandEvaluation(dice, result) {
  // 족보 판정 검증
  const validRanks = ['Ace', 'Triple', 'Straight', 'Strike', 'Slash', 'Storm', 'Pair', 'NoHand'];
  assert(validRanks.includes(result.rank), `Invalid rank: ${result.rank}`);

  // 공격력 검증 (System A: max 60)
  assert(typeof result.power === 'number', 'Power must be number');
  assert(result.power >= 3 && result.power <= 60, 'Power out of range');
}

function validatePurchaseItem(item) {
  // P2W 검증
  assert(item.isCosmeticOnly, 'Non-cosmetic purchase detected');
  assert(!item.affectsGameplay, 'Gameplay-affecting item detected');
  assert(!item.providesAdvantage, 'Advantage-providing item detected');
}
```

### 6.4 일반적인 실수와 해결책

#### 실수 #1: 동시 주사위 굴림
```javascript
// ❌ 잘못됨
async function battle() {
  const [playerDice, enemyDice] = await Promise.all([
    rollDice('player'),
    rollDice('enemy')
  ]);
}

// ✅ 올바름
async function battle() {
  const playerDice = await rollDice('player');
  await processTurn(playerDice, 'player');
  
  const enemyDice = await rollDice('enemy');
  await processTurn(enemyDice, 'enemy');
}
```

#### 실수 #2: 보스 HP를 300으로 설정
```javascript
// ❌ 잘못됨
const boss = {
  hp: 300  // 단일 HP 300 - 잘못됨!
};

// ✅ 올바름 (페이즈 시스템)
const boss = {
  currentPhase: 1,
  maxPhases: 3,
  hp: 100,  // 현재 페이즈 HP
  phases: [
    { phase: 1, hp: 100, pattern: 'testing' },
    { phase: 2, hp: 100, pattern: 'serious' },
    { phase: 3, hp: 100, pattern: 'true_form' }
  ]
};
```

#### 실수 #3: HP를 변수로 설정
```javascript
// ❌ 잘못됨
const enemy = {
  hp: floor * 10  // 층에 따라 HP 변화
};

// ✅ 올바름
const enemy = {
  hp: 100,  // 항상 100
  aiLevel: getAILevel(floor)  // AI로 난이도 조절
};
```

#### 실수 #4: 클라이언트에서 게임 로직
```javascript
// ❌ 잘못됨
function onDiceRoll() {
  const dice = [rand(), rand(), rand()];  // 클라이언트 생성
  const hand = evaluateHand(dice);  // 클라이언트 판정
  dealDamage(hand.power);  // 클라이언트 처리
}

// ✅ 올바름
async function onDiceRoll() {
  const result = await api.post('/battle/roll');  // 서버 요청
  displayResult(result);  // UI 업데이트만
}
```

### 6.5 개발 시작 전 체크리스트

```
코딩 시작 전:
[ ] 이 기능이 핵심 규칙에 위배되는가?
[ ] 사전 승인이 필요한가?
[ ] 테스트 계획이 있는가?
[ ] 관련 스킬 문서를 읽었는가?

코딩 중:
[ ] 네이밍 컨벤션을 따르는가?
[ ] 에러 처리가 적절한가?
[ ] 주석이 명확한가?
[ ] 성능을 고려했는가?

코딩 후:
[ ] 단위 테스트를 작성했는가?
[ ] 검증 함수를 실행했는가?
[ ] 코드 리뷰를 받았는가?

배포 전:
[ ] 모든 테스트가 통과했는가?
[ ] 성능 목표를 달성했는가?
[ ] 보안 체크를 완료했는가?
[ ] 문서를 업데이트했는가?
```

### 6.6 긴급 상황 대응

#### Critical 버그 발견 시
1. **즉시 알림**: 개발 팀에게 Slack #critical-bugs
2. **영향 파악**: 몇 명 영향? 게임 플레이 가능?
3. **핫픽스 작성**: 24시간 내
4. **테스트**: 최소 검증 후 배포
5. **사후 분석**: 근본 원인 파악 및 재발 방지

#### 서버 다운 시
1. **상태 페이지 업데이트**: status.hotelsortis.com
2. **Discord 공지**: #announcements
3. **복구 작업**: 우선순위 높음
4. **보상**: 영혼석 100개 (코스메틱 화폐)
5. **보고서**: 원인 및 대책

---

## 📝 문서 정보

- **문서 버전**: 2.0.0 (개발 지침 중심)
- **최종 수정일**: 2026-01-29
- **대상**: 프로젝트 개발 에이전트
- **목적**: 개발 시 준수해야 할 핵심 규칙 정의

---

**"주사위는 굴려졌다. 하지만 누가 판단하는가?"**
*Alea Iacta Est. Sed Quis Iudicat?*

**핵심 원칙을 기억하세요:**
1. 턴제 시스템 (동시 실행 금지)
2. HP는 항상 100
3. 스킬 슬롯은 4개
4. Pay-to-Win 절대 금지
5. 서버에서 모든 게임 로직 검증