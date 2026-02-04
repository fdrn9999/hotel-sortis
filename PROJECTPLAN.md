# HOTEL SORTIS - Complete Game Design Document
## 호텔 소르티스 - 완전한 게임 기획서

> **"Alea Iacta Est. Sed Quis Iudicat?"**  
> *"주사위는 던져졌다. 그러나 누가 판단하는가?"*

---

## 📑 목차 (Table of Contents)

1. [게임 개요](#1-게임-개요-game-overview)
2. [핵심 게임 시스템](#2-핵심-게임-시스템-core-game-systems)
3. [전투 시스템](#3-전투-시스템-combat-system)
4. [스킬 시스템](#4-스킬-시스템-skill-system)
5. [난이도 시스템](#5-난이도-시스템-difficulty-system)
6. [보스 디자인](#6-보스-디자인-boss-design)
7. [캠페인 구조](#7-캠페인-구조-campaign-structure)
8. [PvP 랭크 모드](#8-pvp-랭크-모드-ranked-pvp-mode)
9. [세계관 및 내러티브](#9-세계관-및-내러티브-worldbuilding--narrative)
10. [아트 디렉션](#10-아트-디렉션-art-direction)
11. [사운드 디자인](#11-사운드-디자인-sound-design)
12. [UI/UX 설계](#12-uiux-설계-uiux-design)
13. [기술 스택 및 아키텍처](#13-기술-스택-및-아키텍처-technical-stack--architecture)
14. [수익 모델](#14-수익-모델-monetization-model)
15. [개발 로드맵](#15-개발-로드맵-development-roadmap)
16. [튜토리얼 및 온보딩](#16-튜토리얼-및-온보딩-tutorial--onboarding)
17. [다국어 지원](#17-다국어-지원-multilingual-support)

---

## 1. 게임 개요 (Game Overview)

### 1.1 기본 정보

| 항목 | 내용 |
|------|------|
| **게임명** | HOTEL SORTIS (호텔 소르티스) |
| **장르** | Turn-based Strategy Roguelike / Skill Build PvP |
| **플랫폼** | Web Browser (WebGL), Cross-platform (PC/Mobile) |
| **타겟 연령** | 18세 이상 |
| **예상 플레이 타임** | PvE 런 당 30-45분, PvP 매치 당 5-10분 |
| **가격 정책** | F2P (Free-to-Play) with Cosmetic-only IAP |
| **지원 언어** | 한국어, 영어, 일본어, 중국어(간체) |

### 1.2 핵심 컨셉

HOTEL SORTIS는 **친치로(Chinchiro) 주사위 게임**을 현대적 로그라이크로 재해석한 전략 게임입니다. 3개의 주사위로 만드는 전통적 족보 시스템에 **최대 4개 스킬 조합**의 무한한 전략적 깊이를 더했습니다.

**핵심 게임 루프:**
```
스킬 선택 (0~4개) → 전투 시작 → 주사위 굴림 → 족보 판정 → 데미지 처리 → 
보스 격파 → 새 스킬 획득 → 빌드 재구성 → 다음 층 진행
```

### 1.3 차별화 포인트

1. **친숙함 + 깊이**: 누구나 아는 주사위 게임이지만, 스킬 시너지로 무한한 전략 창출
2. **공정한 경쟁**: Pay-to-Win 없는 순수 실력 중심 PvP
3. **즉시 접근성**: 웹 기반으로 설치 없이 바로 플레이
4. **PvE ↔ PvP 연계**: 캠페인에서 획득한 스킬을 랭크전에서 활용
4. **PvE ↔ PvP 연계**: 캠페인에서 획득한 스킬을 랭크전에서 활용
5. **다양한 변수 (Roguelike Elements)**: 층별 룰 변형(Mutators)과 랜덤 이벤트
6. **글로벌 서비스**: 4개 언어 지원으로 전 세계 동시 서비스

---

## 2. 핵심 게임 시스템 (Core Game Systems)

### 2.1 족보 시스템 (Poker Hand System)

3개의 6면체 주사위(D6)로 만드는 족보 기반 전투 시스템입니다.

#### 2.1.1 족보 순위 및 공격력

| 순위 | 족보명 (한글) | 족보명 (영문) | 일본어 원명 | 조건 | 공격력 | 확률 |
|------|-------------|--------------|------------|------|--------|------|
| 1 | **에이스** | **Ace** | ピンゾロ | [1-1-1] | **45** | 0.46% |
| 2 | **트리플** | **Triple** | ゾロ目 | 동일 3개 [2-6] | **8 + (숫자×4)** | 2.31% |
| 3 | **스트레이트** | **Straight** | シゴロ | [4-5-6] | **38** | 2.78% |
| 4 | **스트라이크** | **Strike** | - | [3-4-5] | **30** | 2.78% |
| 5 | **슬래시** | **Slash** | - | [2-3-4] | **24** | 2.78% |
| 6 | **스톰** | **Storm** | ヒフミ | [1-2-3] | **16** | 2.78% |
| 7 | **페어** | **Pair** | 出目 | 동일 2개 | **5 + (페어값×2)** | 44.44% |
| 8 | **노 핸드** | **No Hand** | 目なし | 족보 없음 | **합계 (3-16)** | 41.66% |

**공식 예시:**
```javascript
// 트리플 [6-6-6]
power = 8 + (6 × 4) = 32

// 페어 [5-5-2]
power = 5 + (5 × 2) = 15

// 노 핸드 [2-4-6]
power = 2 + 4 + 6 = 12
```

#### 2.1.2 족보 판정 규칙
```javascript
function evaluateHand(dice) {
  const sorted = [...dice].sort();
  const [a, b, c] = sorted;
  
  // 1. 에이스 (Ace): [1-1-1]
  if (a === 1 && b === 1 && c === 1) {
    return {
      rank: 'Ace',
      rankKR: '에이스',
      power: 45
    };
  }

  // 2. 트리플 (Triple): 동일 3개 [2-6]
  if (a === b && b === c && a >= 2) {
    return {
      rank: 'Triple',
      rankKR: '트리플',
      power: 8 + (a * 4)
    };
  }

  // 3. 스트레이트 (Straight): [4-5-6]
  if (a === 4 && b === 5 && c === 6) {
    return {
      rank: 'Straight',
      rankKR: '스트레이트',
      power: 38
    };
  }

  // 4. 스트라이크 (Strike): [3-4-5]
  if (a === 3 && b === 4 && c === 5) {
    return {
      rank: 'Strike',
      rankKR: '스트라이크',
      power: 30
    };
  }

  // 5. 슬래시 (Slash): [2-3-4]
  if (a === 2 && b === 3 && c === 4) {
    return {
      rank: 'Slash',
      rankKR: '슬래시',
      power: 24
    };
  }

  // 6. 스톰 (Storm): [1-2-3]
  if (a === 1 && b === 2 && c === 3) {
    return {
      rank: 'Storm',
      rankKR: '스톰',
      power: 16
    };
  }
  
  // 7. 페어 (Pair): 동일 2개
  if (a === b || b === c) {
    const pairValue = a === b ? a : b;
    return { 
      rank: 'Pair', 
      rankKR: '페어',
      power: 5 + (pairValue * 2) 
    };
  }
  
  // 8. 노 핸드 (No Hand): 합계
  return { 
    rank: 'NoHand', 
    rankKR: '노 핸드',
    power: a + b + c 
  };
}
```

### 2.2 스킬 장착 시스템

#### 2.2.1 기본 규칙

- **슬롯 수**: 최대 **4개** (0~4개 장착 가능)
- **장착 시점**: 전투 시작 전 / 보스 클리어 후
- **효과 발동**: 자동 (패시브)
- **중복 장착**: 불가능 (각 슬롯에 다른 스킬)
- **게임 시작**: 스킬 0개여도 전투 가능

#### 2.2.2 스킬 획득 방식
```
보스 격파
    ↓
희귀도별 3개 제시
    ↓
플레이어가 1개 선택
    ↓
선택한 스킬 + 선택 안 한 2개 모두 영구 언락
    ↓
다음 런에서 사용 가능
```

**보스별 스킬 보상:**
- **5층 (Mammon)**: Rare 스킬 3개 중 1개 선택
- **10층 (Eligor)**: Epic 스킬 3개 중 1개 선택
- **15층 (Lucifuge)**: Legendary 스킬 3개 중 1개 선택

---

## 3. 전투 시스템 (Combat System)

### 3.1 턴제 및 인터랙션 구조 (Turn-based Interaction)

**⚠️ 중요: 절대 동시에 주사위를 굴리지 않습니다!**

#### 3.1.1 상세 전투 및 스킬 발동 흐름
플레이어의 개입 요소를 극대화하기 위해 각 단계별 인터랙션 윈도우를 제공합니다.

```
[턴 시작]
1. 턴 개시 (Turn Start)
   - PASSIVE 효과 적용
   - TURN_START 스킬 발동 (예: "매 턴 체력 5 회복")

[행동 단계]
2. 프리캐스트 (Pre-cast)
   - 플레이어가 '주사위 굴리기 전' 사용할 스킬 선택 (예: "다음 굴림에 주사위 1개 추가")
3. 주사위 굴림 (Roll Dice)
   - 서버에서 결과 생성 + 물리 애니메이션 (3초)
   - 타이머 일시 정지 (애니메이션 중)
4. 리액션 (Reaction) : DICE_ROLL 트리거
   - 굴린 직후 발동하는 스킬 (예: "1을 6으로 변경", "전체 재굴림")
   - 사용자가 결과 확인 후 개입 가능

[판정 단계]
5. 족보 확정 (Hand Evaluation)
   - 최종 주사위 값으로 족보 및 기본 공격력 계산
6. 카운터/방어 (Counter/Defense) : BEFORE_DAMAGE 트리거
   - 데미지 계산 전 스킬 발동 (예: "방어력 증가", "상대 족보 무효화")

[결과 단계]
7. 데미지 계산 및 적용
   - 쉴드(Shield) 우선 소모 → 남은 데미지 HP 차감
8. 포스트 효과 (Post Effect) : AFTER_DAMAGE 트리거
   - 피격 후 발동 (예: "받은 피해의 30% 반사", "스킬 쿨타임 감소")
9. 턴 종료 체크
   - 상태이상 처리, 턴 카운트 증가
```

#### 3.1.2 전투 종료 조건

| 조건 | 결과 |
|------|------|
| 플레이어 HP ≤ 0 | **패배** → 런 종료 |
| 적 HP ≤ 0 | **승리** → 다음 전투 or 보스 |
| 10턴 초과 | **무승부** → 둘 다 생존 처리 |

#### 3.1.3 PvP 턴 시간 제한

**⚠️ 중요: PvP에서는 각 턴마다 30초 시간 제한이 있습니다!**

| 항목 | 내용 |
|------|------|
| **턴 시간** | 30초 |
| **경고** | 10초 남았을 때 UI 경고 |
| **타임아웃** | 자동으로 주사위 굴림 |
| **AI 턴** | 시간 제한 없음 (즉시 행동) |

**타임아웃 처리:**
```javascript
// 플레이어 턴 타임아웃 시
if (timeRemaining <= 0 && currentTurn === 'player') {
  // 자동으로 주사위 굴림
  autoRollDice();
  
  // UI 알림
  showNotification('시간 초과! 자동으로 주사위를 굴렸습니다.');
}
```

### 3.2 HP 및 방어 시스템

#### 3.2.1 기본 규칙 (Fixed HP)

**모든 캐릭터의 HP는 100으로 고정됩니다.**
이는 직관적인 데미지 계산과 벨런싱을 위함입니다. 단, 단순함을 보완하기 위해 **쉴드(Shield)** 시스템을 도입합니다.

```javascript
const HP_CONSTANTS = {
  PLAYER: 100,        // 플레이어 (전투당)
  NORMAL_ENEMY: 100,  // 일반 적
  ELITE_ENEMY: 100,   // 엘리트 적
  BOSS_PER_PHASE: 100 // 보스 (페이즈당)
};
```

#### 3.2.2 쉴드 시스템 (Shield Mechanic)
HP 외에 임시 생명력을 제공하여 전략적 깊이를 더합니다.
- **기능**: 데미지를 입을 때 HP보다 먼저 소모됩니다.
- **지속**: 해당 전투 동안만 유지되거나, 턴 종료 시 소멸될 수 있습니다 (스킬에 따라 다름).
- **최대치**: 제한 없음 (이론상).

```javascript
// 스킬 예시: Iron Will
{
  name: "Iron Will",
  effect: (state) => {
    state.player.shield += 20; // 쉴드 20 획득
  }
}
```

#### 3.2.2 난이도 조절 방식

**❌ 금지: HP로 난이도 조절**
```javascript
// 절대 금지!
const enemy = {
  hp: floor * 20  // 층별 HP 증가
};
```

**✅ 올바름: AI + 보스 페이즈로 난이도 조절**
```javascript
const enemy = {
  hp: 100,  // 항상 100
  aiLevel: getAILevel(floor),  // AI 지능으로 조절
  phases: floor === 15 ? 3 : floor === 10 ? 2 : floor === 5 ? 2 : 1
};
```

### 3.3 데미지 계산 공식
```javascript
function calculateDamage(hand, attackerSkills, defenderSkills, defenderState) {
  let finalDamage = hand.power;
  
  // 1. 공격자 스킬 적용 (BEFORE_DAMAGE)
  for (const skill of attackerSkills) {
    if (skill.trigger === 'BEFORE_DAMAGE') {
      finalDamage = skill.effect(finalDamage, hand);
    }
  }
  
  // 2. 방어자 스킬 적용 (Resistance/Defense)
  for (const skill of defenderSkills) {
    if (skill.type === 'DAMAGE_REDUCTION') {
      finalDamage *= (1 - skill.reduction);
    }
  }

  // 3. 쉴드 차감 로직
  let remainingDamage = Math.floor(finalDamage);
  
  if (defenderState.shield > 0) {
    const shieldDamage = Math.min(defenderState.shield, remainingDamage);
    defenderState.shield -= shieldDamage;
    remainingDamage -= shieldDamage;
  }
  
  // 4. 최소 데미지 보장 (쉴드로 막혔으면 0 가능)
  return Math.max(0, remainingDamage);
}
```

---

## 4. 스킬 시스템 (Skill System)

### 4.1 스킬 구조
```javascript
{
  id: "unique_skill_id",
  name: "Skill Name",
  nameKR: "스킬 이름",
  nameJP: "スキル名",
  nameCN: "技能名称",
  rarity: "Common" | "Rare" | "Epic" | "Legendary",
  description: "효과 설명 (1-2문장)",
  descriptionKR: "한국어 설명",
  descriptionJP: "日本語説明",
  descriptionCN: "中文说明",
  trigger: "BATTLE_START" | "DICE_ROLL" | "BEFORE_DAMAGE" | "AFTER_DAMAGE" | "PASSIVE",
  effect: function(gameState) { /* 구현 */ },
  icon: "path/to/icon.png"
}
```

### 4.2 스킬 트리거 타입

| 트리거 | 발동 시점 | 횟수 |
|--------|----------|------|
| **BATTLE_START** | 전투 시작 시 | 1회 |
| **TURN_START** | 매 턴 시작 시 | 매턴 |
| **DICE_ROLL** | 주사위 굴린 직후 | 매턴 |
| **BEFORE_DAMAGE** | 데미지 계산 전 | 매턴 |
| **AFTER_DAMAGE** | 데미지 처리 후 | 매턴 |
| **PASSIVE** | 항상 적용 | - |

### 4.3 스킬 예시 (60개 중 일부)

#### 4.3.1 Common (기본 제공 3개)
```javascript
{
  id: "lucky_reroll",
  name: "Lucky Reroll",
  nameKR: "행운의 재굴림",
  nameJP: "ラッキーリロール",
  nameCN: "幸运重掷",
  rarity: "Common",
  description: "Automatically reroll 1 die at battle start.",
  descriptionKR: "매 전투 시작 시 주사위 1개를 자동으로 재굴림합니다.",
  descriptionJP: "戦闘開始時にサイコロ1個を自動的に振り直します。",
  descriptionCN: "战斗开始时自动重掷1个骰子。",
  trigger: "BATTLE_START",
  effect: (state) => {
    const rerollIndex = Math.floor(Math.random() * 3);
    state.dice[rerollIndex] = rollSingleDie();
    return state;
  }
}

{
  id: "steady_hand",
  name: "Steady Hand",
  nameKR: "안정된 손",
  nameJP: "安定の手",
  nameCN: "稳定之手",
  rarity: "Common",
  description: "Convert all 1s to 2s after rolling.",
  descriptionKR: "주사위 결과 중 1이 나오면 자동으로 2로 변경됩니다.",
  descriptionJP: "1の目が出たら自動的に2に変更されます。",
  descriptionCN: "将所有1点自动转换为2点。",
  trigger: "DICE_ROLL",
  effect: (state) => {
    state.dice = state.dice.map(d => d === 1 ? 2 : d);
    return state;
  }
}

{
  id: "safe_bet",
  name: "Safe Bet",
  nameKR: "안전한 베팅",
  nameJP: "安全な賭け",
  nameCN: "安全下注",
  rarity: "Common",
  description: "Deal 2x damage with No Hand.",
  descriptionKR: "노 핸드 족보일 때 데미지를 2배로 받습니다.",
  descriptionJP: "役なしの時、ダメージが2倍になります。",
  descriptionCN: "无手牌时造成2倍伤害。",
  trigger: "BEFORE_DAMAGE",
  effect: (state) => {
    if (state.hand.rank === 'NoHand') {
      state.damage *= 2;
    }
    return state;
  }
}
```

#### 4.3.2 Rare (5층 보스 보상)
```javascript
{
  id: "high_roller",
  name: "High Roller",
  nameKR: "하이 롤러",
  nameJP: "ハイローラー",
  nameCN: "豪赌客",
  rarity: "Rare",
  description: "One die always rolls 4 or higher.",
  descriptionKR: "주사위 1개가 항상 4 이상으로 나옵니다.",
  descriptionJP: "サイコロ1個が常に4以上になります。",
  descriptionCN: "1个骰子总是掷出4点或更高。",
  trigger: "DICE_ROLL",
  effect: (state) => {
    const lowestIndex = state.dice.indexOf(Math.min(...state.dice));
    if (state.dice[lowestIndex] < 4) {
      state.dice[lowestIndex] = Math.floor(Math.random() * 3) + 4; // 4-6
    }
    return state;
  }
}

{
  id: "pair_master",
  name: "Pair Master",
  nameKR: "페어 마스터",
  nameJP: "ペアマスター",
  nameCN: "对子大师",
  rarity: "Rare",
  description: "Pair hands deal +10 damage.",
  descriptionKR: "페어 족보 완성 시 공격력 +10 추가.",
  descriptionJP: "ペア役完成時に攻撃力+10。",
  descriptionCN: "对子手牌额外造成+10伤害。",
  trigger: "BEFORE_DAMAGE",
  effect: (state) => {
    if (state.hand.rank === 'Pair') {
      state.damage += 10;
    }
    return state;
  }
}

{
  id: "double_or_nothing",
  name: "Double or Nothing",
  nameKR: "더블 오어 낫씽",
  nameJP: "ダブル・オア・ナッシング",
  nameCN: "全赢或全输",
  rarity: "Rare",
  description: "50% chance to deal 2x damage or 0.",
  descriptionKR: "매 턴 50% 확률로 데미지 2배 또는 0.",
  descriptionJP: "毎ターン50%の確率でダメージ2倍または0。",
  descriptionCN: "每回合50%几率造成2倍伤害或0伤害。",
  trigger: "BEFORE_DAMAGE",
  effect: (state) => {
    state.damage = Math.random() < 0.5 ? state.damage * 2 : 0;
    return state;
  }
}
```

#### 4.3.3 Epic (10층 보스 보상)
```javascript
{
  id: "storm_seeker",
  name: "Storm Seeker",
  nameKR: "스톰 시커",
  nameJP: "ストームシーカー",
  nameCN: "风暴追寻者",
  rarity: "Epic",
  description: "Storm deals +20 damage (total 40).",
  descriptionKR: "[1-2-3] 스톰 완성 시 공격력 +20 추가 (총 40).",
  descriptionJP: "[1-2-3]ストーム完成時に攻撃力+20（合計40）。",
  descriptionCN: "[1-2-3]风暴额外造成+20伤害（总计40）。",
  trigger: "BEFORE_DAMAGE",
  effect: (state) => {
    if (state.hand.rank === 'Storm') {
      state.damage += 20;
    }
    return state;
  }
}

{
  id: "perfect_defense",
  name: "Perfect Defense",
  nameKR: "완벽한 방어",
  nameJP: "完全防御",
  nameCN: "完美防御",
  rarity: "Epic",
  description: "Take 50% less damage from No Hand attacks.",
  descriptionKR: "노 핸드 족보 시 받는 데미지 50% 감소.",
  descriptionJP: "役なし攻撃時に受けるダメージ50%減少。",
  descriptionCN: "受到无手牌攻击时伤害减少50%。",
  trigger: "BEFORE_DAMAGE",
  effect: (state) => {
    if (state.isDefending && state.opponentHand.rank === 'NoHand') {
      state.incomingDamage *= 0.5;
    }
    return state;
  }
}

{
  id: "counter_attack",
  name: "Counter Attack",
  nameKR: "반격",
  nameJP: "カウンター攻撃",
  nameCN: "反击",
  rarity: "Epic",
  description: "Reflect 30% of damage taken back to attacker.",
  descriptionKR: "데미지를 받을 때마다 받은 데미지의 30%를 반격합니다.",
  descriptionJP: "ダメージを受けた時、そのダメージの30%を反撃します。",
  descriptionCN: "受到伤害时反击30%伤害。",
  trigger: "AFTER_DAMAGE",
  effect: (state) => {
    const counterDamage = Math.floor(state.damageTaken * 0.3);
    state.enemy.hp -= counterDamage;
    return state;
  }
}
```

#### 4.3.4 Legendary (15층 보스 보상)
```javascript
{
  id: "fate_manipulation",
  name: "Fate Manipulation",
  nameKR: "운명 조작",
  nameJP: "運命操作",
  nameCN: "命运操控",
  rarity: "Legendary",
  description: "After rolling, change 1 die to any number (1-6).",
  descriptionKR: "주사위를 굴린 후, 원하는 주사위 1개를 1-6 중 원하는 숫자로 변경 가능.",
  descriptionJP: "サイコロを振った後、1個を1~6の好きな数字に変更できます。",
  descriptionCN: "掷骰后，可将1个骰子改为1-6之间的任意数字。",
  trigger: "DICE_ROLL",
  effect: (state) => {
    // UI에서 플레이어 선택 필요
    state.canManipulateDice = true;
    state.manipulateCount = 1;
    return state;
  }
}

{
  id: "probability_distortion",
  name: "Probability Distortion",
  nameKR: "확률 왜곡",
  nameJP: "確率歪曲",
  nameCN: "概率扭曲",
  rarity: "Legendary",
  description: "2x probability for Ace and Triple hands.",
  descriptionKR: "에이스 또는 트리플 확률이 2배로 증가합니다.",
  descriptionJP: "エースまたはトリプルの確率が2倍になります。",
  descriptionCN: "Ace和三同号的概率翻倍。",
  trigger: "PASSIVE",
  effect: (state) => {
    // 주사위 굴림 알고리즘 수정
    // Ace/Triple 가중치 2배
    state.aceTripleProbability *= 2;
    return state;
  }
}

{
  id: "cosmic_hand",
  name: "Cosmic Hand",
  nameKR: "우주의 손",
  nameJP: "宇宙の手",
  nameCN: "宇宙之手",
  rarity: "Legendary",
  description: "Straight or Storm skips enemy's next turn.",
  descriptionKR: "스트레이트 또는 스톰 완성 시 적의 다음 턴을 스킵합니다.",
  descriptionJP: "ストレートまたはストーム完成時に敵の次のターンをスキップします。",
  descriptionCN: "顺子或风暴完成时跳过敌人的下一回合。",
  trigger: "AFTER_DAMAGE",
  effect: (state) => {
    if (state.hand.rank === 'Straight' || state.hand.rank === 'Storm') {
      state.enemy.skipNextTurn = true;
    }
    return state;
  }
}
```

### 4.4 스킬 밸런스 철학

1. **희귀도 ≠ 절대 강함**: Epic이 상황에 따라 Legendary보다 유용할 수 있음
2. **시너지 중시**: 4개 스킬의 조합이 개별 스킬보다 중요
3. **카운터 존재**: 모든 빌드에는 약점이 있어야 함
4. **리스크/리워드**: 높은 보상에는 위험이 따름 (예: Double or Nothing)

---

## 5. 난이도 시스템 (Difficulty System)

### 5.1 AI 레벨

**HP가 아닌 AI 지능으로 난이도를 조절합니다.**

| AI 레벨 | 이름 | 적용 층 | 행동 패턴 | 스킬 수 |
|---------|------|---------|----------|---------|
| **0** | BASIC | 1-3층 | 완전 랜덤 주사위, 스킬 미사용 | 0개 |
| **1** | STANDARD | 4-9층 | 기본 전략 (HP 낮으면 방어) | 1-2개 |
| **2** | ADVANCED | 10-14층 | 족보 예측, 카운터 전략 | 2-3개 |
| **3** | MASTER | 15층 | 최적 플레이, 모든 경우의 수 계산 | 4개 |

### 5.2 AI 구현 예시
```javascript
class EnemyAI {
  constructor(level) {
    this.level = level;
  }

  // Level 0: BASIC
  basicAI(gameState) {
    return { action: 'ROLL' };  // 그냥 굴리기만
  }

  // Level 1: STANDARD
  standardAI(gameState) {
    const { enemyHP } = gameState;
    
    if (enemyHP < 30 && this.hasDefensiveSkill()) {
      return {
        action: 'USE_SKILL',
        skillId: this.getDefensiveSkill()
      };
    }
    
    return { action: 'ROLL' };
  }

  // Level 2: ADVANCED
  advancedAI(gameState) {
    const { playerDiceHistory } = gameState;
    
    // 플레이어 패턴 분석
    const predictedHand = this.predictPlayerHand(playerDiceHistory);
    
    if (predictedHand.power > 100) {
      return this.useDefensiveStrategy();
    } else {
      return this.useOffensiveStrategy();
    }
  }

  // Level 3: MASTER
  masterAI(gameState) {
    // 모든 경우의 수 시뮬레이션
    const outcomes = this.simulateAllOutcomes(gameState);
    
    // 승률 최대화 행동 선택
    return outcomes.reduce((best, current) => 
      current.winRate > best.winRate ? current : best
    );
  }

  decideAction(gameState) {
    switch(this.level) {
      case 0: return this.basicAI(gameState);
      case 1: return this.standardAI(gameState);
      case 2: return this.advancedAI(gameState);
      case 3: return this.masterAI(gameState);
    }
  }
}
```

---

## 6. 보스 디자인 (Boss Design)

### 6.1 보스 페이즈 시스템

**각 보스는 여러 페이즈를 가지며, 페이즈마다 HP 100으로 시작합니다.**

#### 6.1.1 Mammon (5층 중간 보스)

**총 HP: 200 (2페이즈 × 100)**

| 페이즈 | HP | AI 레벨 | 패턴 | 스킬 | 특수 제한 |
|--------|-------|---------|------|------|-----------|
| **Phase 1** | 100 | STANDARD | Aggressive | Greed Dice, Double or Nothing | Storm 사용 불가 |
| **Phase 2** | 100 | ADVANCED | Desperate | + All or Nothing | 제한 해제, 극단적 베팅 |

**컷신:**
```
[Phase 2 전환]
Mammon: "You've forced my hand... ALL IN!"
[금빛 주사위가 검은색으로 변함]
```

#### 6.1.2 Eligor (10층 중간 보스)

**총 HP: 200 (2페이즈 × 100)**

| 페이즈 | HP | AI 레벨 | 패턴 | 스킬 | 특수 능력 |
|--------|-------|---------|------|------|-----------|
| **Phase 1** | 100 | ADVANCED | Defensive | Perfect Defense, Armor Up | 받는 데미지 20% 감소 |
| **Phase 2** | 100 | ADVANCED | Counter | + Counter Attack | 받는 데미지 30% 감소, 50% 반격 |

**컷신:**
```
[Phase 2 전환]
Eligor: "Your attacks... are futile."
[갑옷에서 붉은 빛이 새어나옴]
```

#### 6.1.3 Lucifuge Rofocale (15층 최종 보스)

**총 HP: 300 (3페이즈 × 100)**

| 페이즈 | HP | AI | 패턴 | 주사위 | 특수 주사위 | 스킬 |
|--------|-------|-----|------|--------|-------------|------|
| **Phase 1** | 100 | MASTER | Testing | 6개 | 없음 | Dimensional Dice, Fate Manipulation |
| **Phase 2** | 100 | MASTER | Serious | 7개 | Void, Chaos | + Probability Distortion |
| **Phase 3** | 100 | MASTER | True Form | 9개 | Void, Chaos, Destiny, Paradox, Infinity | + Cosmic Hand |

**특수 주사위 효과:**
- **Void (공허)**: 결과가 0 (노 핸드 보장)
- **Chaos (혼돈)**: 1-6 랜덤 재굴림
- **Destiny (운명)**: 원하는 숫자로 고정
- **Paradox (역설)**: 두 주사위 값 교환
- **Infinity (무한)**: 모든 주사위 6으로 변경

**컷신:**
```
[Phase 2 전환]
Lucifuge: "Impressive. But you've seen nothing yet."
[인간 형태에서 반투명 기하학 형상으로 변환]

[Phase 3 전환]
Lucifuge: "Behold... the true nature of fate."
[9개의 차원 주사위가 궤도를 그리며 회전]
```

### 6.2 페이즈 전환 시스템
```javascript
class BossPhaseManager {
  constructor(bossId) {
    this.config = BOSS_CONFIGS[bossId];
    this.currentPhase = 1;
    this.hp = 100;
  }

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

  async transitionToNextPhase() {
    this.currentPhase++;
    
    // 1초 페이드 아웃
    await fadeOut(1000);
    
    // 페이즈 전환 컷신
    await this.playPhaseTransitionCutscene();
    
    // HP 리셋
    this.hp = 100;
    
    // 새 페이즈 설정 적용
    const phaseConfig = this.config[`phase${this.currentPhase}`];
    this.applyPhaseConfig(phaseConfig);
    
    // UI 업데이트
    this.updatePhaseUI();
    
    // 1초 페이드 인
    await fadeIn(1000);
  }

  applyPhaseConfig(phaseConfig) {
    this.aiLevel = phaseConfig.aiLevel;
    this.skills = phaseConfig.skills;
    this.restrictions = phaseConfig.restrictions;
    this.pattern = phaseConfig.pattern;
  }

  onBossDefeated() {
    playVictoryAnimation();
    unlockSkillReward(this.config.floor);
    saveProgress(this.config.floor);
  }
}
```

---

## 7. 캠페인 구조 (Campaign Structure)

### 7.0 층별 룰 변형 (Floor Mutators)
게임플레이의 단조로움을 막기 위해 층마다 랜덤한 '변형 룰(Anomaly)'이 적용될 수 있습니다. (4층 이상부터 등장)

| 룰 이름 | 효과 | 분위기 |
|---------|------|--------|
| **Heavy Gravity** | 주사위가 무거워져 잘 구르지 않음 (물리 엔진 조정) | 압박감 |
| **Foggy Room** | 상대의 주사위 1개가 보이지 않음 (`?`) | 미스터리 |
| **Silence** | `BATTLE_START` 스킬 발동 불가 | 정적 |
| **Inflated Economy** | 상점 가격 50% 증가하지만 보상 골드도 증가 | 탐욕 |
| **Mirror Room** | `Pair` 족보의 공격력이 2배 | 환각 |

### 7.1 15층 구조 (변경 불가)

| 층 | 유형 | 전투 수 | 보스 | 스킬 보상 |
|-----|------|---------|------|----------|
| 1 | 일반 | 3회 | - | - |
| 2 | 일반 | 3회 | - | - |
| 3 | 일반 | 4회 | - | - |
| 4 | 엘리트 | 1회 | - | - |
| **5** | **보스** | **1회** | **Mammon** | **Rare 1개** |
| 6 | 일반 | 3회 | - | - |
| 7 | 일반 | 4회 | - | - |
| 8 | 일반 | 3회 | - | - |
| 9 | 엘리트 | 1회 | - | - |
| **10** | **보스** | **1회** | **Eligor** | **Epic 1개** |
| 11 | 일반 | 4회 | - | - |
| 12 | 엘리트 | 1회 | - | - |
| 13 | 일반 | 4회 | - | - |
| 14 | 엘리트 | 1회 | - | - |
| **15** | **최종 보스** | **1회** | **Lucifuge** | **Legendary 1개** |

### 7.2 런 진행 흐름
```
[런 시작]
1. 보유 스킬 목록 표시
2. 0~4개 선택 및 장착
3. 장착 완료 버튼 클릭

[1층 시작]
4. 일반 전투 #1 → 승리
5. 일반 전투 #2 → 승리
6. 일반 전투 #3 → 승리

[2-4층 진행]
7. 각 층 전투 완료

[5층 보스]
8. Mammon 등장 (2페이즈)
9. 승리 → Rare 스킬 3개 제시
10. 1개 선택 → 모두 언락
11. 스킬 재장착 (기존 4개 중 교체 가능)

[6-9층 진행]
12. 각 층 전투 완료

[10층 보스]
13. Eligor 등장 (2페이즈)
14. 승리 → Epic 스킬 3개 제시
15. 1개 선택 → 모두 언락
16. 스킬 재장착

[11-14층 진행]
17. 각 층 전투 완료

[15층 최종 보스]
18. Lucifuge 등장 (3페이즈)
19. 승리 → Legendary 스킬 3개 제시
20. 1개 선택 → 모두 언락
21. 엔딩 컷신
22. 크레딧
```

### 7.3 런 실패 시
```
패배 (HP 0)
    ↓
런 종료
    ↓
획득한 스킬은 영구 보존
    ↓
다음 런에서 사용 가능
    ↓
진행도는 초기화 (1층부터 재시작)
```

---

## 8. PvP 랭크 모드 (Ranked PvP Mode)

### 8.1 기본 규칙

| 항목 | 내용 |
|------|------|
| **매칭 방식** | ELO 기반 (±150 범위) |
| **대기 시간** | 30초마다 범위 +50 확대 |
| **최대 대기** | 3분 (범위 무제한) |
| **전투 방식** | 10라운드 또는 HP 0까지 |
| **턴 시간 제한** | 30초 (타임아웃 시 자동 굴림) |
| **승리 보상** | ELO +25, 영혼석 20 |
| **패배 보상** | ELO -25, 영혼석 5 |
| **무승부 보상** | ELO ±0, 영혼석 10 |

### 8.2 매칭 시스템
```javascript
class PvPMatchmaking {
  constructor() {
    this.queue = [];
    this.eloRange = 150;
    this.maxWaitTime = 180000; // 3분
  }

  async findMatch(player) {
    const startTime = Date.now();
    let range = this.eloRange;

    while (Date.now() - startTime < this.maxWaitTime) {
      // 범위 내 상대 검색
      const opponent = this.findOpponent(player.elo, range);
      
      if (opponent) {
        return this.createMatch(player, opponent);
      }

      // 30초마다 범위 확대
      if ((Date.now() - startTime) % 30000 === 0) {
        range += 50;
      }

      await sleep(1000);
    }

    // 3분 초과 시 범위 무제한
    return this.findOpponent(player.elo, Infinity);
  }

  findOpponent(elo, range) {
    return this.queue.find(p => 
      Math.abs(p.elo - elo) <= range
    );
  }

  createMatch(player1, player2) {
    // 큐에서 제거
    this.removeFromQueue(player1);
    this.removeFromQueue(player2);

    // WebSocket으로 매치 생성 알림
    notifyMatchFound(player1, player2);

    // 전투 시작
    return new PvPBattle(player1, player2);
  }
}
```

### 8.3 ELO 계산
```javascript
function calculateELO(winner, loser) {
  const K = 32;  // K-factor
  
  // 예상 승률
  const expectedWin = 1 / (1 + Math.pow(10, (loser.elo - winner.elo) / 400));
  const expectedLose = 1 - expectedWin;
  
  // 새로운 ELO
  winner.elo += K * (1 - expectedWin);
  loser.elo += K * (0 - expectedLose);
  
  return {
    winnerELO: Math.round(winner.elo),
    loserELO: Math.round(loser.elo)
  };
}
```

### 8.4 시즌 시스템

**시즌 기간**: 3개월 (분기별)

**시즌 종료 시:**
1. 랭크별 보상 지급 (코스메틱 아이템)
2. ELO 소프트 리셋: `newELO = (currentELO + 1000) / 2`
3. 새 시즌 스킬 3-5개 추가
4. 새 시즌 시작

**랭크 티어:**
| 티어 | ELO 범위 | 보상 (시즌 종료 시) |
|------|----------|---------------------|
| **Bronze** | 0-999 | 주사위 스킨 1개 |
| **Silver** | 1000-1299 | 주사위 스킨 2개 |
| **Gold** | 1300-1599 | 주사위 스킨 3개 + 아바타 1개 |
| **Platinum** | 1600-1899 | 주사위 스킨 4개 + 아바타 2개 |
| **Diamond** | 1900-2199 | 모든 시즌 코스메틱 |
| **Master** | 2200+ | 독점 타이틀 + 모든 코스메틱 |

---

## 9. 세계관 및 내러티브 (Worldbuilding & Narrative)

### 9.1 세계관 설정

#### 9.1.1 HOTEL SORTIS

**위치**: 현실계(Mundus)와 심연계(Abyssus) 사이의 경계 공간

**시대**: 1920년대 (Art Deco 전성기)

**본질**: 영혼을 확률로 거래하는 차원의 정산소

**규칙**: 물리 법칙과 인과율이 왜곡됨. 주사위는 우주의 인과율을 6면으로 압축한 초월적 상징.

#### 9.1.2 층별 변화

| 층 범위 | 외관 | 분위기 | 컬러 팔레트 |
|---------|------|--------|-------------|
| **1-5층** | 아르데코 로비/카지노 | 화려함, 우아함 | 금색, 크림색, 벨벳 레드 |
| **6-10층** | 균열된 객실/경비실 | 불안, 진실 | 회색, 보라색, 청록색 |
| **11-15층** | 코즈믹 호러 펜트하우스 | 공포, 초월 | 보라색, 검은색, 네온 핑크 |

### 9.2 주요 캐릭터

#### 9.2.1 The Wanderer (주인공)

**외형**: 
- 젖은 회색 트렌치코트
- 검은 셔츠, 낡은 구두
- 얼굴은 항상 그림자에 가려짐

**배경**:
- 폭우를 피해 호텔에 우연히 들어옴
- 과거 기억이 단편적
- 시스템의 예외 존재 (주사위 3개 사용 + 스킬 장착 가능)

**대사 특징**: 
- 무음 (플레이어 몰입을 위해 대사 없음)
- 선택지도 없음 (행동으로만 표현)

#### 9.2.2 Lucifuge Rofocale (최종 보스)

**인간형 (Phase 1-2)**:
- 검은 턱시도, 금테 안경, 올백 헤어
- 30대 중반 신사
- 차분하고 예의 바른 말투

**진형 (Phase 3)**:
- 반투명 보라색 기하학 형상
- 6-9개의 차원 주사위가 궤도를 그림
- 목소리는 다층적 에코

**대사 예시**:
```
"The dice have spoken."
"Fate is absolute."
"You defy probability itself."
"Impressive... but futile."
```

**배경**:
- 호텔 지배인이자 주사위 시스템의 화신
- 영혼 거래의 중재자
- 인과율의 구현체

#### 9.2.3 Mammon (중간 보스 #1)

**외형**:
- 금빛 정장, 금시계, 금반지
- 40대 후반, 배가 나온 체형
- 금빛 형광 주사위 사용

**성격**:
- 탐욕, 도박 중독, 극단적 선택

**대사 예시**:
```
"Risk everything... or lose everything."
"ALL IN!"
"The house... always wins..."
```

#### 9.2.4 Eligor (중간 보스 #2)

**외형**:
- 검은 갑옷 정장
- 검은 금속 주사위 사용
- 30대 초반, 냉철한 표정

**성격**:
- 완벽주의, 방어 전문가, 반격 전략가

**대사 예시**:
```
"Your attacks are futile."
"Defense is absolute."
"I've calculated every possibility."
```

### 9.3 스토리 구조 (3막)

#### Act 1: 환상의 체크인 (1-5층)
```
[오프닝]
폭우 속을 걷는 주인공. 앞에 아르데코 양식의 호텔이 나타남.
"HOTEL SORTIS" 네온사인이 번쩍임.

[로비]
루시푸지가 미소로 맞이함.
"Welcome to HOTEL SORTIS. Checking in?"
주인공이 고개를 끄덕임 → 계약 성립.

[1-4층]
화려한 로비와 카지노. 주사위 게임으로 층을 올라감.
직원들은 친절하지만 미소가 어딘가 이상함.

[5층 보스: Mammon]
"You want to climb higher? Then bet EVERYTHING!"
승리 → 첫 Rare 스킬 획득.
```

#### Act 2: 진실의 폭로 (6-10층)
```
[6층부터]
벽지가 벗겨지고, 기하학적 문양이 드러남.
직원들의 눈에서 붉은 빛이 새어나옴.

[8층]
거울에 비친 자신의 모습이 일그러짐.
호텔의 진실: 영혼을 확률로 거래하는 정산소.

[10층 보스: Eligor]
"You cannot escape. This is your judgment."
승리 → Epic 스킬 획득.
주인공은 시스템의 예외임을 깨달음.
```

#### Act 3: 운명의 재판 (11-15층)
```
[11층부터]
완전히 코즈믹 호러 공간으로 변모.
바닥은 투명하고, 아래로 무한한 보라색 공간.
벽은 끊임없이 움직이는 기하학 패턴.

[14층]
루시푸지의 메시지:
"You've come far. But fate is absolute.
Break the rules, or obey them. Choose."

[15층 최종 보스: Lucifuge]
Phase 1: "Let us test your resolve."
Phase 2: "Impressive. But you've seen nothing."
Phase 3: "Behold... the true nature of fate."

승리 → Legendary 스킬 획득.
규칙의 예외로 인정받아 탈출 성공.

[엔딩]
주인공이 호텔 밖으로 나옴.
뒤를 돌아보니 호텔은 사라짐.
손에는 3개의 주사위만 남음.
```

---

## 10. 아트 디렉션 (Art Direction)

### 10.1 시각적 진화

#### 10.1.1 1-5층: 아르데코 우아함

**컬러 팔레트**:
```css
--primary: #D4AF37;    /* 금색 */
--secondary: #FFFDD0;  /* 크림색 */
--accent-1: #8B0000;   /* 벨벳 레드 */
--accent-2: #50C878;   /* 에메랄드 */
--bg: #1B1B27;         /* 짙은 남색 */
```

**조명**: 
- 따뜻한 샹들리에 조명
- 부드러운 그림자
- 금빛 반사

**재질**:
- 대리석 바닥
- 벨벳 카펫
- 금박 장식

#### 10.1.2 6-10층: 균열과 불안

**컬러 팔레트**:
```css
--primary: #808080;    /* 회색 */
--secondary: #6A0DAD;  /* 보라색 */
--accent-1: #008B8B;   /* 청록색 */
--bg: #0D0D0D;         /* 검은색 */
```

**시각 효과**:
- 벽지가 벗겨진 부분에서 보라색 빛
- 깨진 거울
- 왜곡된 그림자

#### 10.1.3 11-15층: 코즈믹 호러

**컬러 팔레트**:
```css
--primary: #6A0DAD;    /* 보라색 */
--secondary: #000000;  /* 검은색 */
--accent-1: #FF10F0;   /* 네온 핑크 */
--accent-2: #7DF9FF;   /* 전기 블루 */
--glow: #8A2BE2;       /* 보라 글로우 */
```

**시각 효과**:
- 투명한 바닥 (아래로 무한 공간)
- 끊임없이 움직이는 기하학 패턴
- 보라색 글로우 효과
- 반투명 재질

### 10.2 캐릭터 디자인

#### 10.2.1 The Wanderer
```
[실루엣]
- 키: 175cm
- 체형: 평범
- 자세: 약간 구부정

[의상]
- 젖은 회색 트렌치코트 (무릎까지)
- 검은 셔츠 (단추 2개 풀림)
- 짙은 청색 바지
- 낡은 갈색 구두

[얼굴]
- 항상 그림자에 가려짐 (플레이어 투영)
- 눈만 약간 보임 (푸른빛)

[컬러 팔레트]
- 주조색: #5A5A5A (회색)
- 보조색: #1C1C1C (검은색)
- 악센트: #4682B4 (푸른빛 눈)
```

#### 10.2.2 Lucifuge Rofocale

**인간형 (Phase 1-2)**:
```
[의상]
- 검은 턱시도 (완벽한 핏)
- 흰색 셔츠 (금단추)
- 금테 안경
- 올백 헤어 (검은색)

[특징]
- 30대 중반, 신사적
- 미소는 예의 바르지만 눈은 차가움
- 손에는 항상 주사위 1개

[컬러 팔레트]
- 주조색: #000000 (검은색)
- 보조색: #FFFFFF (흰색)
- 악센트: #D4AF37 (금색)
```

**진형 (Phase 3)**:
```
[형태]
- 반투명 기하학 형상
- 인간 실루엣의 왜곡
- 6-9개의 주사위가 궤도를 그림

[효과]
- 보라색 글로우
- 끊임없이 변화하는 패턴
- 다층적 에코 목소리

[컬러 팔레트]
- 주조색: #6A0DAD (보라색, 70% 투명)
- 보조색: #FF10F0 (네온 핑크)
- 악센트: #7DF9FF (전기 블루)
```

### 10.3 주사위 디자인

#### 10.3.1 기본 주사위
```
재질: 상아색 플라스틱
크기: 16mm × 16mm × 16mm
점: 검은색 (오목)
모서리: 약간 둥글게

[물리]
- Three.js Cannon.js 사용
- 현실적 굴림 물리
- 탁자 충돌 소리
```

#### 10.3.2 특수 주사위 (보스 전용)

**Void (공허)**:
```
재질: 검은 유리
점: 없음 (완전 검은색)
효과: 검은 안개
```

**Chaos (혼돈)**:
```
재질: 금이 간 수정
점: 랜덤 색상 변화
효과: 번쩍이는 불꽃
```

**Destiny (운명)**:
```
재질: 금색 금속
점: 다이아몬드
효과: 금빛 궤적
```

**Paradox (역설)**:
```
재질: 거울
점: 반대편 숫자
효과: 공간 왜곡
```

**Infinity (무한)**:
```
재질: 투명 보라색 크리스탈
점: 무한 기호 (∞)
효과: 보라 글로우
```

---

## 11. 사운드 디자인 (Sound Design)

> **⚠️ Critical Requirement**: 사운드는 본 게임의 '타격감'과 '분위기'를 결정하는 핵심 요소이므로, 선택 사항이 아닌 **필수 사항**으로 개발합니다.

### 11.1 BGM 구성

| 구간 | 분위기 | 악기 구성 | BPM | 참고 |
|------|--------|----------|-----|------|
| **메인 메뉴** | 미스테리, 우아함 | 피아노, 첼로, 앰비언트 | 70-80 | Bioshock 메인 테마 |
| **1-5층** | 1920s 재즈 스윙 | 트럼펫, 색소폰, 더블베이스 | 120-140 | Cuphead OST |
| **6-10층** | 긴장감, 불안 | 스트링, 불협화음, 드럼 | 90-110 | Silent Hill OST |
| **11-15층** | 코즈믹 호러 | 신시사이저, 드론, 역재생 | 60-70 | Darkest Dungeon OST |
| **보스 전투** | 오케스트라 사사 | 풀 오케스트라 + 전자음 | 140-160 | Dark Souls Boss Theme |

### 11.2 효과음 (SFX)

#### 11.2.1 주사위 관련
```
dice_roll.wav
- 나무 탁자에 주사위 구르는 소리
- 0.5-2.0초 길이
- 물리 기반 (속도에 따라 음높이 변화)

dice_land.wav
- 주사위 멈추는 소리
- 0.1초
- 탁!

dice_select.wav
- 주사위 선택 시 (스킬 사용)
- 부드러운 클릭

**Interactive Audio Feedback**:
- 주사위를 드래그할 때 덜그럭거리는 소리 (Shake)
- 강하게 던지면 강한 충돌음, 약하게 던지면 약한 소리 (Velocity-based)
```

#### 11.2.2 족보 완성
```
ace_complete.mp3
- 최고 음높이 (C6)
- 반짝이는 마법 소리
- 2초

triple_complete.mp3
- 높은 음 (A5-B5)
- 폭발적인 소리
- 1.5초

straight_complete.mp3
- 상승하는 음계
- 1.5초

storm_complete.mp3
- 바람 소리 + 천둥
- 2초

pair_complete.mp3
- 중간 음 (E4)
- 부드러운 벨 소리
- 1초

nohand.mp3
- 낮은 음 (C3)
- 실망스러운 소리
- 0.5초
```

#### 11.2.3 전투 관련
```
damage_dealt.wav
- 공격 성공 (타격음)
- 크리티컬 시 더 강한 소리

damage_taken.wav
- 피격음
- HP 비례 음높이 변화

hp_low.wav
- HP 30 이하 시 심장 박동 소리 루프

victory.mp3
- 승리 팡파르
- 3초

defeat.mp3
- 패배 비극적 음악
- 2초
```

#### 11.2.4 스킬 발동
```
skill_activate_common.wav
- 부드러운 윙~ 소리
- 0.5초

skill_activate_rare.wav
- 마법 충전 소리
- 1초

skill_activate_epic.wav
- 강력한 폭발음
- 1.5초

skill_activate_legendary.wav
- 우주적 웅웅거림
- 2초
```

### 11.3 보이스 디렉션

#### 11.3.1 Lucifuge Rofocale

**음성 특징**:
- 낮고 차분한 남성 보이스 (바리톤)
- 영어 (영국식 억양)
- Phase 3에서는 다층적 에코 효과

**대사 예시** (음성 녹음):
```
"Welcome to HOTEL SORTIS."
"The dice have spoken."
"Fate is absolute."
"You defy probability itself."
"Impressive... but futile."
"Behold... the true nature of fate."
```

#### 11.3.2 시스템 안내

**음성 특징**:
- 중성적 AI 보이스
- 한국어/영어/일본어/중국어 선택 가능
- 명확하고 또렷한 발음

**안내 예시**:
```
한국어: "스킬 발동" / "족보 완성: 에이스" / "전투 승리"
영어: "Skill Activated" / "Hand Complete: Ace" / "Victory"
일본어: "スキル発動" / "役完成：エース" / "勝利"
중국어: "技能发动" / "手牌完成：Ace" / "胜利"
```

### 11.4 사운드 구현 체크리스트
- [ ] Howler.js 등 오디오 라이브러리 도입
- [ ] 볼륨 조절 (BGM, SFX, Voice) 및 음소거 기능
- [ ] 3D 오디오 (Three.js PositionalAudio) 적용 - 주사위 위치에 따른 소리 방향감

---

## 12. UI/UX 설계 (UI/UX Design)

### 12.1 핵심 설계 원칙

1. **3초 규칙**: 첫 화면에서 3초 내에 게임 방식 이해 가능
2. **정보 명확성**: 모든 수치와 효과를 명확히 표시
3. **크로스 플랫폼**: PC/Mobile 모두 최적화
4. **접근성**: 색맹 모드, 폰트 크기 조절 지원
5. **다국어 지원**: 4개 언어 완벽 지원

### 12.2 메인 화면 구조
```
┌─────────────────────────────────────┐
│  [홈]  [←]  [언어]        [설정]     │  ← 네비게이션 바
├─────────────────────────────────────┤
│                                     │
│        HOTEL SORTIS                 │
│    ─────────────────                │
│                                     │
│     ┌──────────────┐               │
│     │  PvE 캠페인   │               │
│     └──────────────┘               │
│                                     │
│     ┌──────────────┐               │
│     │  PvP 랭크     │               │
│     └──────────────┘               │
│                                     │
│     ┌──────────────┐               │
│     │  컬렉션       │               │
│     └──────────────┘               │
│                                     │
│     ┌──────────────┐               │
│     │  상점        │               │
│     └──────────────┘               │
│                                     │
└─────────────────────────────────────┘
```

### 12.3 전투 화면 레이아웃
```
┌─────────────────────────────────────┐
│  [홈]  [←]  Floor 5  [설정]          │
├─────────────────────────────────────┤
│                                     │
│  적 HP: ████████░░ 80/100          │
│                                     │
│         [적 주사위]                 │
│          ? ? ?                      │
│                                     │
│      ┌─────────────┐               │
│      │  전투 로그   │               │
│      │  Player: 에이스 │             │
│      │  Damage: 60  │               │
│      └─────────────┘               │
│                                     │
│      [플레이어 주사위]              │
│        🎲 🎲 🎲                     │
│                                     │
│  플레이어 HP: ████████████ 100/100  │
│                                     │
│  ┌────┐ ┌────┐ ┌────┐ ┌────┐      │
│  │스킬1│ │스킬2│ │스킬3│ │스킬4│      │
│  └────┘ └────┘ └────┘ └────┘      │
│                                     │
│     [주사위 굴리기] (Space)          │
│     ⏱️ 30초 남음                    │
│                                     │
└─────────────────────────────────────┘
```

### 12.4 스킬 장착 화면
```
┌─────────────────────────────────────┐
│  [홈]  [←]  스킬 장착  [설정]         │
├─────────────────────────────────────┤
│                                     │
│  보유 스킬 (12개)     장착 슬롯 (4개)│
│  ┌──────────────┐   ┌────┐         │
│  │ 행운의 재굴림 │ → │ 1  │         │
│  │ [Common]     │   └────┘         │
│  └──────────────┘                   │
│                    ┌────┐         │
│  ┌──────────────┐   │ 2  │         │
│  │ 하이 롤러    │   └────┘         │
│  │ [Rare]       │                   │
│  └──────────────┘   ┌────┐         │
│                    │ 3  │         │
│  ┌──────────────┐   └────┘         │
│  │ 스톰 시커    │                   │
│  │ [Epic]       │   ┌────┐         │
│  └──────────────┘   │ 4  │         │
│                    └────┘         │
│  ...                                │
│                                     │
│  장착 수: 3/4                       │
│        [전투 시작] (Enter)           │
│                                     │
└─────────────────────────────────────┘
```

### 12.5 모달 시스템 (alert 금지)
```vue
<!-- CustomModal.vue -->
<template>
  <Teleport to="body">
    <div v-if="isOpen" class="modal-overlay" @click="close">
      <div class="modal-container" @click.stop>
        <h2>{{ t(title) }}</h2>
        <p>{{ t(message) }}</p>
        
        <div class="modal-buttons">
          <button @click="confirm">{{ t('common.confirm') }}</button>
          <button v-if="type === 'confirm'" @click="cancel">
            {{ t('common.cancel') }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>
```

### 12.6 반응형 디자인

#### 12.6.1 PC (1920×1080 이상)
```css
.game-container {
  max-width: 1200px;
  margin: 0 auto;
}

.dice-area {
  width: 800px;
  height: 600px;
}

.skill-slots {
  display: flex;
  gap: 20px;
}
```

#### 12.6.2 모바일 (세로 모드)
```css
.game-container {
  width: 100%;
  padding: 16px;
}

.dice-area {
  width: 100%;
  height: 50vh;
}

.skill-slots {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

/* 터치 최소 크기 */
.btn {
  min-width: 44px;
  min-height: 44px;
}
```

### 12.7 키보드 단축키 (PC)

| 키 | 기능 |
|----|------|
| **Space** | 주사위 굴리기 |
| **1-4** | 스킬 슬롯 선택 |
| **Esc** | 설정 열기/닫기 |
| **H** | 홈으로 |
| **M** | 음소거 토글 |
| **F** | 전체화면 토글 |
| **L** | 언어 변경 |

---

## 13. 기술 스택 및 아키텍처 (Technical Stack & Architecture)

### 13.1 기술 스택

#### 13.1.1 프론트엔드
```json
{
  "framework": "Vue 3.4+",
  "composition": "Composition API + <script setup>",
  "state": "Pinia",
  "router": "Vue Router 4",
  "build": "Vite 5+",
  "3d": "Three.js r160+",
  "physics": "Cannon.js",
  "websocket": "STOMP.js + SockJS",
  "i18n": "Vue I18n 9+"
}
```

#### 13.1.2 백엔드
```json
{
  "framework": "Spring Boot 3.2+",
  "language": "Java 17+",
  "database": "MariaDB 11.x",
  "cache": "Redis 7.x",
  "orm": "Spring Data JPA",
  "api": "Spring HATEOAS (RESTful)",
  "websocket": "Spring WebSocket (STOMP)"
}
```

#### 13.1.3 인프라
```json
{
  "cloud": "GCP (Google Cloud Platform)",
  "storage": "Cloud Storage",
  "cdn": "Cloud CDN",
  "compute": "Compute Engine",
  "monitoring": "Cloud Monitoring"
}
```

### 13.2 시스템 아키텍처
```
┌────────────────────────────────────────┐
│          클라이언트 (Vue 3)             │
│  ┌──────────┐  ┌──────────────────┐   │
│  │ Three.js │  │ STOMP WebSocket  │   │
│  │ (주사위)  │  │ (PvP 실시간)     │   │
│  └──────────┘  └──────────────────┘   │
│  ┌──────────┐                         │
│  │ Vue I18n │  (다국어 지원)          │
│  └──────────┘                         │
└────────────────┬───────────────────────┘
                 │ HTTPS / WSS
┌────────────────▼───────────────────────┐
│       API Gateway (Spring Boot)        │
│  ┌──────────────┐  ┌──────────────┐   │
│  │ REST API     │  │ WebSocket    │   │
│  │ (HATEOAS)    │  │ (STOMP)      │   │
│  └──────────────┘  └──────────────┘   │
└────────────┬───────────────┬───────────┘
             │               │
┌────────────▼──────┐  ┌────▼──────────┐
│  MariaDB          │  │  Redis        │
│  (영구 데이터)     │  │  (세션/캐시)  │
└───────────────────┘  └───────────────┘
```

### 13.3 데이터베이스 스키마

#### 13.3.1 ERD (Entity-Relationship Diagram)
```
┌─────────────┐       ┌──────────────┐
│   players   │───┬───│player_skills │
└─────────────┘   │   └──────────────┘
      │           │
      │           ├───┌────────────┐
      │           │   │   skills   │
      │           │   └────────────┘
      │           │
      │           └───┌──────────────────┐
      ├───────────────│campaign_progress │
      │               └──────────────────┘
      │
      └───────────────┌─────────────┐
                      │   battles   │
                      └─────────────┘
                           │
                           └───┌──────────────┐
                               │ battle_logs  │
                               └──────────────┘
```

#### 13.3.2 테이블 정의

**players (플레이어)**
```sql
CREATE TABLE players (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(20) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  elo INT NOT NULL DEFAULT 1000,
  soul_stones INT NOT NULL DEFAULT 0,
  current_floor INT NOT NULL DEFAULT 1,
  highest_floor_cleared INT NOT NULL DEFAULT 0,
  equipped_skill_ids JSON,  -- [1,2,3,4] (최대 4개)
  preferred_language ENUM('ko', 'en', 'ja', 'zh') NOT NULL DEFAULT 'en',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_username (username),
  INDEX idx_elo (elo)
);
```

**skills (스킬)**
```sql
CREATE TABLE skills (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  skill_code VARCHAR(50) NOT NULL UNIQUE,
  name_en VARCHAR(100) NOT NULL,
  name_ko VARCHAR(100) NOT NULL,
  name_ja VARCHAR(100) NOT NULL,
  name_zh VARCHAR(100) NOT NULL,
  rarity ENUM('Common', 'Rare', 'Epic', 'Legendary') NOT NULL,
  description_en TEXT NOT NULL,
  description_ko TEXT NOT NULL,
  description_ja TEXT NOT NULL,
  description_zh TEXT NOT NULL,
  trigger_type ENUM('BATTLE_START', 'DICE_ROLL', 'BEFORE_DAMAGE', 'AFTER_DAMAGE', 'PASSIVE') NOT NULL,
  effect_json JSON NOT NULL,
  icon_url VARCHAR(255),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_rarity (rarity)
);
```

**player_skills (플레이어-스킬 연결)**
```sql
CREATE TABLE player_skills (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  player_id BIGINT NOT NULL,
  skill_id BIGINT NOT NULL,
  unlocked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
  FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE,
  UNIQUE KEY uk_player_skill (player_id, skill_id),
  INDEX idx_player_id (player_id)
);
```

**battles (전투)**
```sql
CREATE TABLE battles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  player_id BIGINT NOT NULL,
  enemy_id BIGINT,  -- AI면 NULL, PvP면 상대 플레이어 ID
  battle_type ENUM('PVE', 'PVP') NOT NULL,
  floor INT,  -- PvE만
  status ENUM('ONGOING', 'VICTORY', 'DEFEAT', 'DRAW') NOT NULL DEFAULT 'ONGOING',
  player_hp INT NOT NULL DEFAULT 100,
  enemy_hp INT NOT NULL DEFAULT 100,
  turn_count INT NOT NULL DEFAULT 1,
  current_turn ENUM('PLAYER', 'ENEMY') NOT NULL DEFAULT 'PLAYER',
  player_equipped_skills JSON,  -- [1,2,3,4] (최대 4개)
  enemy_equipped_skills JSON,
  started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ended_at TIMESTAMP,
  FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
  INDEX idx_player_id (player_id),
  INDEX idx_status (status)
);
```

**battle_logs (전투 로그)**
```sql
CREATE TABLE battle_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  battle_id BIGINT NOT NULL,
  turn_number INT NOT NULL,
  actor ENUM('PLAYER', 'ENEMY') NOT NULL,
  dice_result JSON NOT NULL,  -- [3,5,2]
  hand_rank VARCHAR(20) NOT NULL,
  hand_rank_kr VARCHAR(20) NOT NULL,  -- 한글 족보명
  hand_power INT NOT NULL,
  damage_dealt INT NOT NULL,
  skills_activated JSON,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (battle_id) REFERENCES battles(id) ON DELETE CASCADE,
  INDEX idx_battle_id (battle_id)
);
```

**campaign_progress (캠페인 진행도)**
```sql
CREATE TABLE campaign_progress (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  player_id BIGINT NOT NULL UNIQUE,
  current_floor INT NOT NULL DEFAULT 1,
  floor_1_cleared BOOLEAN DEFAULT FALSE,
  floor_2_cleared BOOLEAN DEFAULT FALSE,
  -- ... floor_15_cleared
  total_runs INT NOT NULL DEFAULT 0,
  total_victories INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE
);
```

### 13.4 RESTful API 설계 (HATEOAS)

#### 13.4.1 플레이어 API

**GET /api/v1/players/{playerId}**
```json
{
  "playerId": 12345,
  "username": "player_name",
  "elo": 1250,
  "soulStones": 500,
  "currentFloor": 7,
  "highestFloorCleared": 10,
  "preferredLanguage": "ko",
  "_links": {
    "self": {
      "href": "/api/v1/players/12345"
    },
    "skills": {
      "href": "/api/v1/players/12345/skills"
    },
    "campaign": {
      "href": "/api/v1/campaigns/12345"
    },
    "pvp-match": {
      "href": "/api/v1/pvp/matchmaking"
    }
  }
}
```

#### 13.4.2 전투 API

**POST /api/v1/battles** (전투 시작)
```json
// Request
{
  "playerId": 12345,
  "battleType": "PVE",
  "floor": 5,
  "equippedSkills": [1, 2, 3, 4]  // 최대 4개
}

// Response
{
  "battleId": 67890,
  "playerId": 12345,
  "enemyId": null,
  "currentTurn": "PLAYER",
  "playerHp": 100,
  "enemyHp": 100,
  "turnCount": 1,
  "status": "ONGOING",
  "_links": {
    "self": {
      "href": "/api/v1/battles/67890"
    },
    "roll-dice": {
      "href": "/api/v1/battles/67890/roll"
    },
    "surrender": {
      "href": "/api/v1/battles/67890/surrender"
    }
  }
}
```

**POST /api/v1/battles/{battleId}/roll** (주사위 굴림)
```json
// Request
{
  "playerId": 12345
}

// Response (서버에서 주사위 생성!)
{
  "dice": [3, 5, 2],
  "hash": "abc123...",  // 변조 방지 해시
  "hand": {
    "rank": "NoHand",
    "rankKR": "노 핸드",
    "power": 10
  },
  "damage": 10,
  "enemyHp": 90,
  "_links": {
    "self": {
      "href": "/api/v1/battles/67890"
    },
    "wait-enemy-turn": {
      "href": "/api/v1/battles/67890/status"
    }
  }
}
```

### 13.5 WebSocket (STOMP) 구성

#### 13.5.1 Spring Boot WebSocket 설정
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트로 메시지 전송 prefix
        config.enableSimpleBroker("/topic", "/queue");
        
        // 클라이언트에서 서버로 메시지 전송 prefix
        config.setApplicationDestinationPrefixes("/app");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS();
    }
}
```

#### 13.5.2 PvP 실시간 메시지 처리
```java
@Controller
public class PvPWebSocketController {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    // 주사위 굴림
    @MessageMapping("/pvp/battles/{battleId}/roll")
    @SendToUser("/queue/battle-result")
    public BattleActionResultDto rollDice(
        @DestinationVariable Long battleId,
        @Payload RollDiceDto dto
    ) {
        // 서버에서 주사위 생성
        int[] dice = battleService.rollDice(battleId, dto.getPlayerId());
        
        // 족보 판정
        HandResult hand = battleService.evaluateHand(dice);
        
        // 데미지 계산
        int damage = battleService.calculateDamage(hand);
        
        // 상대방에게 전송
        messagingTemplate.convertAndSend(
            "/topic/pvp/battles/" + battleId,
            new BattleUpdateDto(dice, hand, damage)
        );
        
        return new BattleActionResultDto(dice, hand, damage);
    }
}
```

#### 13.5.3 Vue 3 WebSocket 클라이언트
```javascript
// composables/useWebSocket.js
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
      onConnect: (frame) => {
        connected.value = true;
      }
    });
    
    stompClient.value.activate();
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
  
  onMounted(() => connect());
  onUnmounted(() => stompClient.value?.deactivate());
  
  return { connected, subscribe, send };
}
```

---

## 14. 수익 모델 (Monetization Model)

### 14.1 기본 방침

**HOTEL SORTIS는 Pay-to-Win 요소를 완전히 배제합니다.**

#### 14.1.1 절대 금지 목록

- ❌ 스킬 구매 기능
- ❌ 확률 조작 아이템
- ❌ 스탯 부스터
- ❌ 경험치 부스터
- ❌ 부활 아이템
- ❌ PvP 어드밴티지 아이템

#### 14.1.2 허용 (코스메틱만)

- ✅ 주사위 스킨
- ✅ 아바타
- ✅ BGM 팩
- ✅ 이모트
- ✅ 프로필 배지

### 14.2 가격 정책

| 항목 | 가격 (USD) | 내용 |
|------|-----------|------|
| **기본 플레이** | 무료 (F2P) | 모든 게임 컨텐츠 |
| **시즌 패스** | $9.99 | 코스메틱 전용, 스킬 획득 속도 증가 없음 |
| **주사위 스킨** | $2.99 - $9.99 | 재질, 색상, 효과 변경 |
| **아바타** | $4.99 - $14.99 | 주인공 외형 변경 |
| **BGM 팩** | $3.99 | 대체 음악 테마 |

### 14.3 시즌 패스

**가격**: $9.99 (3개월)

**혜택**:
- 독점 주사위 스킨 5개
- 독점 아바타 2개
- 전투 승리 시 영혼석 +50% (코스메틱 화폐)
- 프리미엄 이모트 팩

---

## 15. 개발 로드맵 (Development Roadmap)

### Phase 1-9 (완료)
- 핵심 시스템, 전투, 3D 주사위, 스킬 엔진, PvP 매칭, 코스메틱 상점 구현 완료.

### Phase 10: 폴리싱 & 사운드 (완료)
- **사운드 시스템**: BGM, SFX (Howler.js + Web Audio API 합성음)
- **UI/UX 개선**: 페이지 트랜지션, 반응형 레이아웃, 터치 제스처

### Phase 11: 튜토리얼 & 온보딩 (완료)
- **Floor 0**: 튜토리얼 매치 (스크립트된 전투, Lucifuge 대사)
- **가이드북**: HandGuide 컴포넌트 (Tab 키로 토글)
- **연습 모드**: PracticeView (시간 제한 없는 샌드박스)

### Phase 12: 콘텐츠 확장 (완료 - 2026-02-03)
- **Shield System**: 쉴드 메커니즘 구현 (HP보다 먼저 소모)
- **Floor Mutators**: 5개 효과 구현 (gravity, fog, silence, chaos, endurance)
- **Vue 주사위 리팩토링**: Three.js → Vue CSS 애니메이션 (번들 ~700KB 감소)
- **PvP 드래프트 모드**: 스네이크 드래프트 (A→B→B→A→A→B→B→A)

### Phase 13: 2차 폴리싱 (진행 예정)
- **i18n 정합성**: 4개 locale 파일 키 diff 검증
- **하드코딩 문자열**: ON/OFF 토글, 언어 선택 드롭다운 i18n 전환
- **console.log 정리**: 40+ 건 제거 또는 DEV 가드
- **접근성 개선**: aria-label, 키보드 탭 순서 점검

---

## 16. 튜토리얼 및 온보딩 (Tutorial & Onboarding)

### 16.1 진입 장벽 완화 전략
게임의 독특한 규칙(System A)을 플레이어가 자연스럽게 익히도록 설계합니다.

#### 16.1.1 0층: 더 로비 (Floor 0: The Lobby)
- **개요**: 실제 게임 시작 전, 호텔 지배인(Lucifuge)과 진행하는 튜토리얼 매치.
- **구성**:
    1. **기본 굴리기**: 주사위 3개를 굴려 족보가 만들어지는 과정 시각적으로 강조.
    2. **족보 교육**: `Strike`, `Slash` 등 특수 족보가 떴을 때 "이것이 스트라이크입니다!" 팝업 설명.
    3. **스킬 사용**: 강제로 스킬 카드를 쥐어주고 사용해보게 함.
    4. **승리 보장**: 어떻게 해도 플레이어가 이기도록 설계.

#### 16.1.2 족보 가이드북 (Hand Guide)
- **접근성**: 인게임 전투 화면 어디서든 `?` 버튼이나 `Tab` 키로 호출 가능.
- **내용**: 8개 족보의 구성과 공격력, 확률을 한눈에 볼 수 있는 표.
- **동적 표시**: 현재 내 주사위로 만들 수 있는 족보를 하이라이트.

#### 16.1.3 연습 모드 (Practice Mode)
- **시간 제한 없음**: 30초 룰이 적용되지 않는 AI 대전.
- **샌드박스**: 원하는 스킬을 장착해보고 데미지를 실험할 수 있는 공간.

---

## 17. 다국어 지원 (Multilingual Support)

### 17.1 지원 언어
- **한국어 (Korean)**: 기본 개발 언어
- **영어 (English)**: 글로벌 표준
- **일본어 (Japanese)**: 로그라이크/TCG 주요 시장
- **중국어 (Simplified Chinese)**: 최대 시장

### 17.2 구현 방식
- **Frontend**: `vue-i18n` 사용 (JSON 기반 키 관리)
- **Backend**: DB 컬럼 분리 (`name_ko`, `name_en`...) 및 클라이언트 `Accept-Language` 헤더에 따른 동적 DTO 변환
- **Font**: Noto Sans CJK (모든 언어 대응)