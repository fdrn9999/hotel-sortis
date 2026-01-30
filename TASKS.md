# TASKS.md - 작업 추적 문서

> 최종 업데이트: 2026-01-30

---

## ✅ 완료된 작업 (Completed)

### Phase 1: 프로젝트 초기 설정
- [x] 프로젝트 디렉토리 구조 생성 (`frontend/`, `backend/`)
- [x] Vue 3 + Vite + TypeScript 프론트엔드 설정
- [x] Pinia 상태관리 설정
- [x] Vue Router 4 라우팅 설정
- [x] Vue I18n 다국어 지원 (ko, en, ja, zh)
- [x] Three.js, Cannon-es 의존성 추가
- [x] Spring Boot 3.2 백엔드 프로젝트 생성
- [x] Gradle 빌드 시스템 설정
- [x] MariaDB, Redis 의존성 설정
- [x] CLAUDE.md 개발 지침서 작성

### Phase 2: 핵심 전투 시스템
- [x] 족보 판정 로직 구현 (`HandEvaluator.java`)
- [x] Player, Skill, Battle JPA 엔티티 생성
- [x] BattleRepository 생성
- [x] BattleService 구현 (주사위 굴림, 턴 처리)
- [x] BattleController REST API 구현 (HATEOAS)
- [x] WebSocket 설정 (STOMP)
- [x] 프론트엔드 API 클라이언트 (`api/client.ts`)
- [x] 전투 API 서비스 (`api/battle.ts`)
- [x] BattleView.vue 전투 UI 구현
- [x] 30초 턴 타이머 구현
- [x] 주사위 애니메이션 구현
- [x] 오프라인 모드 (서버 없이 테스트 가능)

### Phase 3: 문서화 및 데이터베이스
- [x] dbinit.md 데이터베이스 설정 가이드
- [x] create.sql 테이블 생성 스크립트 (DDL) - 10개 테이블
- [x] insert.sql 초기 데이터 스크립트 (DML) - **스킬 60개**, 층 15개, 보스 3개
- [x] TASKS.md 작업 추적 문서
- [x] README.md 업데이트
- [x] CLAUDE.md 개발 지침서 업데이트

### Phase 3.5: i18n 전면 지원 + System A 통합 + 코드 리팩토링
- [x] DB 스키마 다국어 컬럼 추가 (`name_ko`, `name_en`, `name_ja`, `name_zh`, `desc_ko`, `desc_en`, `desc_ja`, `desc_zh`)
- [x] insert.sql 60개 스킬 4개 언어 완전 번역
- [x] 프론트엔드 i18n locale 파일 60개 스킬 키 추가 (ko, en, ja, zh)
- [x] System A (PROJECTPLAN.md 기준) 족보 공격력 통합
  - Ace [1-1-1] = 60, Triple [X-X-X] = 10+(X×5), Straight [4-5-6] = 50
  - **Strike [3-4-5] = 40**, **Slash [2-3-4] = 30** (신규 족보 추가)
  - Storm [1-2-3] = 20, Pair [X-X-Y] = 5+(X×2), NoHand = 합계
- [x] insert.sql 스킬 밸런스 수치 System A 기준 조정 (23개 스킬)
- [x] CLAUDE.md 테스트/상수 섹션 System A 기준 수정
- [x] `HandEvaluator.java` - System A 공격력 + Strike/Slash 추가
- [x] `game.ts` 타입 - HandRank에 Strike/Slash 추가
- [x] `battle.ts` store - evaluateHand() System A 통합
- [x] `BattleView.vue` - evaluateHand() System A 통합

---

## 🔄 진행 중 (In Progress)

_현재 진행 중인 작업 없음_

### Phase 4: 3D 주사위 시스템 - 완료
- [x] Three.js 씬 설정
- [x] 3D 주사위 모델 생성 (BoxGeometry)
- [x] Cannon-es 물리 엔진 연동
- [x] 주사위 굴림 애니메이션
- [x] BattleView.vue에 3D 씬 통합
- [x] 주사위 결과 감지 로직 (서버 결과와 동기화는 추후 작업)

### Phase 3.6: 밸런스 조정 (Balance Update) - 완료
- [x] 족보 데미지 ~75% 수준으로 하향 (게임 길이 6-8턴 목표)
  - Ace: 60 → 45
  - Triple: 10+(X×5) → 8+(X×4) = 16-32
  - Straight: 50 → 38
  - Strike: 40 → 30
  - Slash: 30 → 24
  - Storm: 20 → 16
- [x] 스킬 보너스 수치 하향 조정 (~60-70%)
- [x] 누락된 스킬 3개 추가 (calm_focus, lucky_streak, strike_master)
- [x] 백엔드 HandEvaluator.java 업데이트
- [x] 프론트엔드 battle.ts 업데이트
- [x] 모든 문서 업데이트 (PROJECTPLAN, CLAUDE, README, TASKS, dbinit)
- [x] i18n 4개 언어 파일 업데이트

---

## 📋 예정된 작업 (Planned)

### Phase 4: 3D 주사위 시스템
- [ ] Three.js 씬 설정
- [ ] 3D 주사위 모델 생성
- [ ] Cannon-es 물리 엔진 연동
- [ ] 주사위 굴림 애니메이션
- [ ] 주사위 결과 감지 로직

### Phase 5: 스킬 시스템
- [ ] 스킬 효과 엔진 구현 (백엔드)
- [ ] 스킬 발동 트리거 처리
- [ ] 스킬 장착 UI
- [ ] 스킬 선택 화면
- [ ] 스킬 효과 시각화

### Phase 6: 캠페인 모드
- [ ] 15층 구조 구현
- [ ] 층별 적 AI 레벨 설정
- [ ] 보스 페이즈 시스템 (Mammon, Eligor, Lucifuge)
- [ ] 보스 컷신
- [ ] 스킬 보상 시스템
- [ ] 캠페인 진행도 저장

### Phase 7: PvP 시스템
- [ ] ELO 매칭 시스템
- [ ] WebSocket 실시간 대전
- [ ] PvP 전용 UI
- [ ] 랭크 티어 시스템
- [ ] 시즌 보상

### Phase 8: 사용자 시스템
- [ ] 회원가입/로그인
- [ ] JWT 인증
- [ ] 프로필 관리
- [ ] 설정 저장

### Phase 9: 코스메틱 & 상점
- [ ] 주사위 스킨 시스템
- [ ] 아바타 시스템
- [ ] 상점 UI
- [ ] 영혼석 재화 시스템

### Phase 10: 폴리싱
- [ ] 사운드 디자인 (BGM, SFX)
- [ ] UI/UX 개선
- [ ] 반응형 모바일 최적화
- [ ] 성능 최적화
- [ ] 버그 수정

---

## 🐛 알려진 이슈 (Known Issues)

| ID | 설명 | 우선순위 | 상태 |
|----|------|----------|------|
| - | 현재 알려진 이슈 없음 | - | - |

---

## 📝 메모

### 족보 시스템 (System A - PROJECTPLAN.md 기준)

| 순위 | 족보 | 조건 | 공격력 |
|------|------|------|--------|
| 1 | Ace (에이스) | [1-1-1] | 45 |
| 2 | Triple (트리플) | [X-X-X] (2-6) | 8 + X×4 (16-32) |
| 3 | Straight (스트레이트) | [4-5-6] | 38 |
| 4 | Strike (스트라이크) | [3-4-5] | 30 |
| 5 | Slash (슬래시) | [2-3-4] | 24 |
| 6 | Storm (스톰) | [1-2-3] | 16 |
| 7 | Pair (페어) | [X-X-Y] | 5 + X×2 (7-17) |
| 8 | NoHand (노 핸드) | 없음 | 합계 (3-16) |

### 스킬 밸런스 스케일 (Balanced)
- Common: +1~3 범위
- Rare: +3~7 범위
- Epic: +6~12 범위
- Legendary: +10~18 범위

### 기술 스택
- **Frontend**: Vue 3.4+, Vite 5+, Pinia, Vue Router 4, Vue I18n 9+, Three.js, Cannon-es
- **Backend**: Spring Boot 3.2+, Java 17+, MariaDB 11.x, Redis 7.x
- **Protocol**: REST (HATEOAS), WebSocket (STOMP)

### 핵심 규칙 (CLAUDE.md 참조)
- ❌ 클라이언트에서 주사위 생성 금지
- ❌ HP는 항상 100 고정
- ❌ 스킬 슬롯 최대 4개
- ❌ 동시 주사위 굴림 금지 (턴제)
- ✅ 30초 턴 타이머 필수
