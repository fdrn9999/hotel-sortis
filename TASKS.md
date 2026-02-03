# TASKS.md - 작업 추적 문서

> 최종 업데이트: 2026-02-03 (Phase 12 Part 2 완료: Vue 기반 주사위 리팩토링)

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

### Phase 3.6: 밸런스 조정 (Balance Update)
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

### Phase 4: 주사위 시스템
- [x] ~~Three.js 씬 설정~~ (Phase 12 Part 2에서 Vue UI로 대체)
- [x] ~~3D 주사위 모델 생성 (BoxGeometry)~~ (Phase 12 Part 2에서 Vue UI로 대체)
- [x] ~~Cannon-es 물리 엔진 연동~~ (Phase 12 Part 2에서 Vue UI로 대체)
- [x] 주사위 굴림 애니메이션 (Vue CSS 기반)
- [x] BattleView.vue에 주사위 컴포넌트 통합
- [x] 주사위 결과 감지 로직
- [x] **주사위 눈금(Pip) 표시** - CSS 기반 pip 렌더링
- [x] **서버 결과 동기화** - `rollTo()` 메서드로 서버 주사위 값과 애니메이션 일치
- [x] BattleView.vue에서 `rollTo()` 호출 (서버 API 먼저 → 애니메이션 동기화)

### Phase 5: 스킬 시스템

- [x] 스킬 효과 엔진 구현 (백엔드)
  - [x] SkillTrigger enum (BATTLE_START, DICE_ROLL, BEFORE_DAMAGE, AFTER_DAMAGE, PASSIVE)
  - [x] GameState 클래스 (스킬이 조작하는 게임 상태)
  - [x] SkillEffect 인터페이스
  - [x] SkillEffectException (에러 처리)
  - [x] SkillEffectEngine (스킬 실행 엔진)
  - [x] SkillEffectConfiguration (자동 등록)
  - [x] 구체적인 스킬 효과 5개 구현 (LuckyReroll, SteadyHand, SafeBet, HighRoller, PairMaster)
- [x] 스킬 발동 트리거 처리 (BattleService 통합)
  - [x] SkillEffectEngine 주입
  - [x] GameState 생성 헬퍼 메서드
  - [x] 스킬 ID 파싱 메서드
  - [x] rollDice()에 트리거 추가 (BATTLE_START, DICE_ROLL, BEFORE_DAMAGE, AFTER_DAMAGE)
  - [x] processEnemyTurn()에도 동일하게 적용
- [x] 스킬 목록 조회 API (백엔드 i18n 지원)
  - [x] SkillDto (사용자 언어에 맞게 변환)
  - [x] SkillRepository (스킬 조회 쿼리)
  - [x] SkillService (언어 검증 및 변환)
  - [x] SkillController (Accept-Language 헤더 처리)
- [x] 스킬 장착 UI (프론트엔드)
  - [x] 스킬 API 클라이언트 (api/skill.ts)
  - [x] Skill 타입 수정 (백엔드 응답 형식 맞춤)
  - [x] Skill Store (Pinia - 장착/해제/검증)
  - [x] SkillLoadoutView 컴포넌트 (4개 슬롯, 희귀도 필터, 아르데코 스타일)
- [x] i18n 번역 키 추가 (ko, en, ja, zh)
- [x] 스킬 선택 화면 (프론트엔드)
  - [x] SkillSelectionView.vue 컴포넌트 (보스 클리어 후 3개 스킬 선택)
  - [x] i18n 4개 언어 번역 (skillSelection 섹션)
- [x] 스킬 효과 시각화 (프론트엔드)
  - [x] SkillEffectNotification.vue 컴포넌트 (스킬 발동 알림)
  - [x] useSkillEffectNotifications.ts composable (알림 큐 관리)
  - [x] i18n 4개 언어 번역 (skillEffects.trigger 섹션)

### Phase 6: 캠페인 모드
- [x] Backend: Floor, Boss, CampaignProgress, PlayerSkill 엔티티 생성
- [x] Backend: Battle.java에 bossId, bossPhase 필드 추가
- [x] Backend: FloorRepository, BossRepository, CampaignProgressRepository, PlayerSkillRepository 생성
- [x] Backend: BattleRepository에 countByPlayerIdAndFloorAndStatus 추가
- [x] Backend: SkillRepository에 findUnownedByRarity 쿼리 추가
- [x] Backend: CampaignDto.java (7개 DTO 클래스)
- [x] Backend: BattleDto에 보스 관련 필드 추가
- [x] Backend: CampaignService 구현 (진행도, 층 시작, 전투 완료, 스킬 보상)
- [x] Backend: BattleService에 보스 페이즈 전환 로직 추가
- [x] Backend: CampaignController (5개 엔드포인트, HATEOAS)
- [x] Frontend: types/game.ts에 캠페인 관련 타입 추가
- [x] Frontend: api/campaign.ts API 클라이언트 생성
- [x] Frontend: stores/campaign.ts Pinia 스토어 생성 (오프라인 지원)
- [x] Frontend: api/battle.ts에 보스 관련 필드 추가
- [x] Frontend: CampaignView.vue 완전 재작성 (15층 타워, 진행도 표시)
- [x] Frontend: BattleView.vue에 캠페인 모드 지원 (보스 페이즈, 스킬 보상)
- [x] Frontend: SkillLoadoutView.vue에 캠페인 컨텍스트 추가
- [x] Frontend: router/index.ts 캠페인 라우트 추가
- [x] i18n: 4개 언어 파일에 campaign, floors, bosses 섹션 추가 (ko, en, ja, zh)
- [x] 리팩토링: BossId enum 도입 (타입 안전성 향상)
  - [x] BossId.java enum 클래스 생성 (MAMMON, ELIGOR, LUCIFUGE)
  - [x] CampaignService에서 BossId enum 사용
  - [x] HandRank enum 비교 버그 수정 (PairMasterEffect, SafeBetEffect)
  - [x] BossIdTest.java 단위 테스트 추가

### Phase 7: PvP 시스템
- [x] Backend: MatchmakingService (Redis 기반 ELO 매칭)
- [x] Backend: EloCalculator (ELO 계산, K-factor 32)
- [x] Backend: PvPController (REST API, HATEOAS)
  - [x] POST /api/v1/pvp/matchmaking/join
  - [x] GET /api/v1/pvp/matchmaking/find/{playerId}
  - [x] POST /api/v1/pvp/matchmaking/leave
  - [x] GET /api/v1/pvp/rank/{playerId}
- [x] Backend: PvPDto.java (13개 DTO 클래스)
- [x] Backend: PvPWebSocketController (실시간 메시지 핸들러)
  - [x] 주사위 결과 브로드캐스트
  - [x] 턴 동기화
  - [x] 전투 종료 처리 및 ELO 업데이트
- [x] Backend: BattleService.createPvPBattle (PvP 전투 생성)
- [x] Frontend: types/game.ts에 PvP 타입 추가 (13개 타입)
- [x] Frontend: api/pvp.ts API 클라이언트
- [x] Frontend: composables/usePvPWebSocket.ts (WebSocket composable)
- [x] Frontend: views/PvPMatchmakingView.vue (매칭 대기 화면)
  - [x] 실시간 대기 시간 표시
  - [x] ELO 범위 확대 시각화
  - [x] WebSocket 기반 매치 알림
- [x] Frontend: views/RankView.vue (랭크 정보 화면)
  - [x] 티어 배지 및 아이콘
  - [x] ELO, 전적, 승률 표시
  - [x] 티어별 색상 및 이펙트
- [x] Frontend: router/index.ts PvP 라우트 추가
- [x] i18n: 4개 언어 파일에 pvp 섹션 추가 (ko, en, ja, zh)
- [x] **Phase 7 개선사항** (2026-02-01)
  - [x] Redis Lua Script로 매칭 동시성 문제 해결 (원자적 연산)
  - [x] 재접속 처리 구현 (진행 중인 PvP 전투 체크)
  - [x] PvPView.vue를 PvP 대시보드로 전환
  - [x] i18n: viewRankDetails, eloRange 키 추가 (4개 언어)
  - [x] 컴파일 에러 수정 (Battle.Type → Battle.BattleType)

### Phase 8: 사용자 시스템
- [x] Backend: User 엔티티 및 UserRepository 생성
- [x] Backend: JWT 토큰 유틸리티 구현 (JwtTokenProvider)
- [x] Backend: Spring Security 설정 (SecurityConfig, CustomUserDetailsService, JwtAuthenticationFilter)
- [x] Backend: 회원가입/로그인 API (AuthController, AuthService, AuthDto)
- [x] Backend: 프로필 관리 API (UserController, UserService, UserDto)
- [x] Backend: User-Player 1:1 연결 (회원가입 시 자동 Player 생성)
- [x] Backend: build.gradle에 Spring Security, JWT 의존성 추가
- [x] Backend: application.yml에 JWT 설정 추가
- [x] DB: users 테이블 생성 (email, password, role, is_active, email_verified)
- [x] DB: players 테이블에 user_id 외래키 추가
- [x] Frontend: Auth API 클라이언트 (api/auth.ts)
- [x] Frontend: Auth Pinia Store (stores/auth.ts, localStorage 토큰 저장)
- [x] Frontend: api/client.ts에 JWT 토큰 자동 첨부 (Authorization 헤더)
- [x] Frontend: LoginView.vue (아르데코 스타일)
- [x] Frontend: SignupView.vue (이메일, 사용자명, 비밀번호, 선호 언어)
- [x] Frontend: ProfileView.vue (프로필 조회/수정, 비밀번호 변경, 로그아웃)
- [x] Frontend: router/index.ts에 인증 라우트 추가 (/login, /signup, /profile)
- [x] Frontend: 네비게이션 가드 구현 (requiresAuth, guestOnly)
- [x] i18n: 4개 언어 파일에 auth, profile 섹션 추가 (ko, en, ja, zh)

### Phase 8.1: alert() 제거 (CLAUDE.md 3.3.1 규칙 준수)

- [x] Toast 컴포넌트 생성 (components/Toast.vue)
  - [x] 3가지 타입 지원 (success, error, info)
  - [x] 아르데코 스타일 적용
  - [x] 자동 3초 후 사라짐 + 애니메이션
  - [x] 여러 개 동시 표시 가능 (큐 시스템)
- [x] useNotification composable 생성 (composables/useNotification.ts)
  - [x] success(), error(), info() 헬퍼 함수
  - [x] CustomEvent 기반 전역 통신
- [x] App.vue에 Toast 컴포넌트 추가
- [x] LoginView.vue alert() 제거 (2개)
- [x] SignupView.vue alert() 제거 (6개)
- [x] ProfileView.vue alert() 제거 (7개)
- [x] SkillLoadoutView.vue alert() 제거 (5개)
- [x] PvPMatchmakingView.vue confirm() TODO 주석 추가
- [x] 총 20개 alert() 호출 제거 완료
- [x] **TODO (Phase 10)**: confirm() 대체 모달 시스템 구현 ✅
  - ProfileView.vue 로그아웃 확인 (1개)
  - PvPMatchmakingView.vue 매칭 취소 확인 (1개)

### Phase 9: 코스메틱 & 상점 시스템

- [x] Database Schema (docs/create.sql, docs/insert.sql)
  - [x] dice_skins 테이블 생성 (4개 언어 + Three.js 재질 속성)
  - [x] avatars 테이블 생성 (4개 언어)
  - [x] player_cosmetics 테이블 생성 (소유/장착 추적)
  - [x] players 테이블에 equipped_dice_skin_id, equipped_avatar_id 추가
  - [x] 10개 주사위 스킨 데이터 삽입 (4개 언어 완전 번역)
  - [x] 5개 아바타 데이터 삽입 (4개 언어 완전 번역)
- [x] Backend Entities & Repositories
  - [x] DiceSkin.java 엔티티 (Three.js 재질 속성 포함)
  - [x] Avatar.java 엔티티
  - [x] PlayerCosmetic.java 엔티티
  - [x] Player.java에 cosmetic 필드 추가
  - [x] DiceSkinRepository, AvatarRepository, PlayerCosmeticRepository
- [x] Backend DTOs & Services
  - [x] CosmeticDto.java (7개 DTO 클래스, i18n 지원)
  - [x] ShopDto.java (5개 DTO 클래스, i18n 지원)
  - [x] CosmeticService.java (컬렉션 조회, 장착/해제)
  - [x] ShopService.java (상점 조회, 구매, 영혼석 관리)
- [x] Backend Controllers (HATEOAS)
  - [x] CosmeticController.java (5개 엔드포인트)
  - [x] ShopController.java (3개 엔드포인트)
- [x] Frontend Types & API Clients
  - [x] types/game.ts에 cosmetic 타입 추가 (DiceSkin, Avatar, CosmeticType 등)
  - [x] api/cosmetic.ts API 클라이언트
  - [x] api/shop.ts API 클라이언트
- [x] Frontend Stores (Pinia)
  - [x] stores/cosmetic.ts (컬렉션 관리, 장착/해제)
  - [x] stores/shop.ts (상점 아이템, 구매, 영혼석)
- [x] 3D Dice Skin Integration
  - [x] DiceScene.ts 수정 (createDiceFaceTextures에 skin 파라미터)
  - [x] applySkin() 메서드 추가 (런타임 스킨 변경)
  - [x] getCurrentSkin() getter 추가
- [x] Frontend Views
  - [x] ShopView.vue 스켈레톤 생성 (네비게이션 버튼, 로딩 상태, 밸런스 패널)
- [x] i18n 번역 (4개 언어)
  - [x] shop 섹션 추가 (ko, en, ja, zh)
  - [x] cosmetics 섹션 추가 (ko, en, ja, zh)

### Phase 10: 폴리싱 (Polishing & Sound)

- [x] confirm() 모달 시스템 (CLAUDE.md 3.3.1 규칙 준수)
  - ConfirmModal.vue, useConfirmModal.ts 구현
  - ProfileView.vue, PvPMatchmakingView.vue에서 사용 중
- [x] 네비게이션 시스템 (CLAUDE.md 3.3.2 규칙 100% 준수)
  - AppNavigation.vue 공통 컴포넌트 생성
  - 7개 화면 수정 (ProfileView, CollectionView, RankView, PvPView, PvPMatchmakingView, BattleView, HomeView)
  - 모든 화면에 홈/뒤로가기/설정 버튼 추가
- [x] 설정 화면 (CLAUDE.md 3.3.3 규칙 100% 준수)
  - SettingsView.vue 생성 (사운드, 진동, 그래픽, 게임플레이, 언어 설정)
  - settings Pinia store 생성 (localStorage 저장)
  - /settings 라우트 추가
- [x] UI/UX 개선
  - [x] LoadingSpinner.vue 공통 로딩 컴포넌트 생성 (주사위 애니메이션)
  - [x] Vue Router 페이지 전환 트랜지션 (page-fade)
  - [x] 버튼 hover/active/disabled/focus-visible 상태 일관성 (main.css)
  - [x] 커스텀 스크롤바 스타일링
  - [x] 텍스트 선택 색상 통일
- [x] 반응형 모바일 최적화
  - [x] 터치 제스처 지원 (useGestures.ts - 좌측 엣지 스와이프 뒤로가기)
  - [x] SettingsView 모바일 레이아웃 최적화
- [x] 키보드 단축키 (CLAUDE.md 3.3.5 규칙 준수)
  - [x] useKeyboardShortcuts.ts (ESC, H, Backspace, M)
- [x] 코드 스플리팅 (모든 라우트 lazy-loaded 확인)
- [x] **사운드 시스템 구현 (Howler.js + Web Audio API)**
  - [x] Howler.js 의존성 추가
  - [x] useSound.ts composable 생성 (SFX, BGM 매니저)
  - [x] Web Audio API 합성음 SFX 구현 (에셋 파일 없이 즉시 동작)
    - 주사위: diceRoll, diceLand
    - 족보: aceComplete, tripleComplete, straightComplete, stormComplete, pairComplete, noHandComplete
    - 전투: damageDealt, damageTaken, victory, defeat, phaseTransition
    - 스킬: skillActivate (희귀도별 4종)
    - UI: buttonClick, buttonHover, turnChange, timerWarning
  - [x] BGM 플레이어 (Howler.js, 크로스페이드, 루핑, 에셋 없으면 silent fail)
  - [x] BattleView.vue 사운드 통합 (14개 SFX 포인트)
  - [x] HomeView.vue 메뉴 BGM + 버튼 클릭 SFX
  - [x] SettingsView.vue 볼륨 조절 연동 (watch 기반)
  - [x] App.vue 전역 사운드 초기화 (useSound)
  - [x] public/sounds/bgm, public/sounds/sfx 디렉토리 생성 (에셋 준비)
- [x] **버그 수정 (14개 TypeScript 에러 전부 해결)**
  - [x] ApiClient에 query params 지원 추가 (get, delete에 options 파라미터)
  - [x] api/cosmetic.ts, api/shop.ts, api/skill.ts 인자 불일치 해결 (7개)
  - [x] 미사용 import/변수 정리: battle.ts, AppNavigation.vue, ConfirmModal.vue, router, BattleView.vue, PvPMatchmakingView.vue (7개)
  - [x] vue-tsc --noEmit 0 에러, npm run build 성공

### Phase 11: 튜토리얼 및 온보딩

- [x] **Floor 0: The Lobby (튜토리얼 매치)**
  - [x] useTutorial.ts composable 구현 (스텝 엔진, 대화, 스크립트된 주사위)
    - 22단계 튜토리얼 스크립트 (대화, 하이라이트, 주사위 굴림, 적 턴 등)
    - TUTORIAL_DICE_SCRIPTS: 3개 스크립트된 주사위 (플레이어 승리 보장)
    - localStorage 기반 진행도 저장
  - [x] TutorialOverlay.vue (화살표 가이드, 말풍선, 하이라이트)
    - Teleport-to-body 오버레이, 타이프라이터 효과, ResizeObserver
    - z-index 9000-9003 레이어링, box-shadow cutout 기법
  - [x] TutorialView.vue (Floor 0 전투 화면)
    - 3D + 2D 주사위 표시, Lucifuge 대사, 스크립트된 전투 진행
    - evaluateHand() 로컬 구현 (오프라인 모드)
    - Tab 키로 HandGuide 열기
  - [x] Lucifuge 대사 연동 (4개 언어 번역)
- [x] **가이드북 시스템**
  - [x] HandGuide.vue 컴포넌트 (족보표 8종, 확률, 주사위 예시)
    - 현재 주사위에 매칭되는 족보 하이라이트
    - 희귀도별 색상 코딩
    - 반응형: 모바일에서 확률 컬럼 숨김
  - [x] 인게임 오버레이/팝업 (? 버튼 및 Tab 키로 토글)
- [x] **연습 모드 (Practice Mode)**
  - [x] PracticeView.vue (시간 제한 없는 샌드박스 모드)
    - 무제한 주사위 굴림, 족보별 통계 추적
    - 최근 10회 히스토리, 관찰 빈도 퍼센트 계산
    - Space 키 단축키, Tab 키 HandGuide 토글
    - 리셋 버튼으로 통계 초기화
- [x] **i18n 4개 언어 지원 (ko, en, ja, zh)**
  - [x] tutorial 섹션 (lucifuge 대사 7개 + guide 텍스트 9개)
  - [x] handGuide 섹션 (7개 키)
  - [x] practice 섹션 (9개 키)
  - [x] menu.tutorial, menu.practice 키 추가
  - [x] battle.rolling 키 추가
- [x] **라우팅 및 네비게이션**
  - [x] /tutorial, /practice 라우트 추가 (requiresAuth: false)
  - [x] HomeView.vue에 서브 메뉴 (튜토리얼, 연습 모드 버튼)
- [x] vue-tsc --noEmit 0 에러, npm run build 성공

---

## 🔄 진행 중 (In Progress)

### Phase 12 Part 2: Mutator/Draft Mode 완성

- [ ] Backend Mutator 엔티티 및 BattleService 로직 통합
- [ ] Frontend Mutator UI (BattleView, CampaignView)
- [ ] PvP 드래프트 모드 기획 및 구현

---

## ✅ 완료된 작업 (최근)

### Phase 12 Part 1: 쉴드 시스템 및 Mutator 데이터 (2026-02-02)

- [x] **쉴드(Shield) 시스템**
  - [x] Backend: Battle.java에 playerShield, enemyShield 필드 추가
  - [x] Backend: BattleService.java 데미지 계산 로직 수정 (쉴드 우선 소모)
  - [x] Backend: BattleDto.java, GameState.java에 shield 필드 추가
  - [x] Database: battles 테이블에 player_shield, enemy_shield 컬럼 추가
  - [x] Frontend: types/game.ts, api/battle.ts에 shield 타입 추가
  - [x] Frontend: BattleView.vue에 shield UI 추가 (Art Deco 스타일 쉴드 바)
  - [x] 쉴드 관련 스킬 6개 추가 (iron_will, shield_bash, reflective_barrier, barrier_master, shield_surge, fortify)
  - [x] i18n: 4개 언어에 battle.shield 키 추가
- [x] **층별 룰 변형 (Floor Mutators) - 데이터 계층**
  - [x] Database: mutators 테이블 생성 (4개 언어 지원)
  - [x] Database: floors 테이블에 mutator_id 컬럼 추가
  - [x] 5가지 기본 Mutator 데이터 추가:
    - gravity: 주사위 1-2를 3으로 변환
    - fog: 족보 이름 숨김
    - silence: 스킬 발동 확률 50% 감소
    - chaos: 매 턴 주사위 1개 랜덤 재굴림
    - endurance: HP 150으로 시작
  - [x] 층별 Mutator 할당 (4층, 6층, 8층, 9층, 11층, 12층, 13층, 14층)
- [x] vue-tsc --noEmit 0 에러, npm run build 성공

### Phase 12 Part 2: Vue 기반 주사위 리팩토링 (2026-02-03)

- [x] **Three.js + Cannon-es → Vue UI 마이그레이션**
  - [x] DiceRoller.vue 컴포넌트 생성 (CSS 애니메이션 기반)
  - [x] useDiceRoller.ts composable 생성 (기존 useDiceScene API 호환)
  - [x] BattleView.vue 업데이트 (DiceRoller 사용)
  - [x] TutorialView.vue 업데이트 (DiceRoller 사용)
  - [x] PracticeView.vue 업데이트 (DiceRoller 사용)
  - [x] 코스메틱 스킨 지원 (baseColor, pipColor, emissiveColor 적용)
- [x] **의존성 정리**
  - [x] three, cannon-es, @types/three 패키지 제거
  - [x] DiceScene.ts, useDiceScene.ts 삭제
  - [x] game/ 디렉토리 삭제
- [x] **번들 크기 약 700KB 감소** (Three.js ~500KB + Cannon-es ~200KB)
- [x] vue-tsc --noEmit 0 에러, 타입 체크 성공

---

## 📋 예정된 작업 (Planned)

### Phase 12 Part 2: 콘텐츠 확장 완성 (Content Expansion)

- [ ] **Mutator 백엔드 로직**
  - [x] Floor.java에 mutatorId 필드 추가 (2026-02-02)
  - [ ] Mutator 엔티티 및 Repository 생성
  - [ ] MutatorService 생성 (BattleService 분리)
  - [ ] Floor API에 mutator 정보 포함
- [ ] **Mutator 프론트엔드 UI**
  - [ ] BattleView.vue에 현재 Mutator 표시
  - [ ] CampaignView.vue에 층별 Mutator 아이콘 표시
  - [ ] i18n: mutators 섹션 4개 언어 번역
- [ ] **Draft Mode (PvP)**
  - [ ] 스킬 드래프트 시스템 기획 및 구현
- [x] **코드 리팩토링 (일부 완료)** ✅ 2026-02-03
  - [x] BattleView.vue 중복 주사위 UI 제거 (DiceRoller + 2D Dice Display 중복 해결)
  - [x] BattleView.vue 오프라인 로직 분리: useOfflineBattle.ts composable 추출
  - [x] BattleView.vue `as any` 타입 안전성 문제 해결 (SkillRewardOption → Skill 매핑)
  - [ ] BattleView.vue 분리: useBattleState.ts composable 추출 (~1000줄 감소)
  - [ ] BattleService.java 분리: MutatorService, BattleEffectProcessor 추출

### Phase 13: 2차 폴리싱 (2nd Polishing)

- [x] **i18n 정합성 수정 (CRITICAL)** ✅ 2026-02-03
  - [x] ko.json `pvp` 키 중복 병합 완료
  - [x] en.json, ja.json, zh.json 동일하게 `pvp` 키 구조 통일 완료
  - [ ] 4개 locale 파일 전체 키 diff 검증 (누락/불일치 찾기)
  - [ ] `soulStones` 키 네스팅 위치 4개 파일 일관성 맞추기
- [ ] **하드코딩 문자열 i18n 전환**
  - [ ] SettingsView.vue: ON/OFF 토글 텍스트 → `t('common.on')` / `t('common.off')`
  - [ ] SignupView.vue: 언어 선택 드롭다운 라벨 → i18n 키 사용
  - [ ] ProfileView.vue: 언어 선택 드롭다운 라벨 → i18n 키 사용
  - [ ] i18n locale 4개 파일에 `common.on`, `common.off` 키 추가
- [ ] **console.log/error 정리 (40+ 건)**
  - [ ] `stores/skill.ts` — 8건 제거 또는 `import.meta.env.DEV` 가드
  - [ ] `stores/cosmetic.ts` — 12건 제거
  - [ ] `stores/shop.ts` — 10건 제거
  - [ ] `stores/auth.ts` — console.error 1건 정리
  - [ ] `api/client.ts` — console.error 1건 정리
  - [ ] `game/DiceScene.ts` — console.log 2건 제거
  - [ ] `composables/usePvPWebSocket.ts` — WebSocket 디버그 로그 정리
  - [ ] `views/BattleView.vue` — 전투 디버그 로그 정리
- [ ] **UI/UX 빈 상태(Empty State) 및 에러 상태 보강**
  - [ ] ShopView.vue: 에러 상태 UI 추가 (현재 없음)
  - [ ] ShopView.vue: 빈 상점 상태 UI 개선 (현재 placeholder 텍스트)
  - [ ] CollectionView.vue: 빈 컬렉션 상태 메시지 추가
  - [ ] RankView.vue: 빈 랭크 데이터 상태 메시지 추가
  - [ ] PvPView.vue: 에러/로딩 상태 점검
- [ ] **접근성(Accessibility) 개선**
  - [ ] ShopView.vue: 이모지 전용 버튼에 aria-label 추가
  - [ ] HomeView.vue: 이모지 전용 아이콘 버튼에 aria-label 보강
  - [ ] 모든 form input에 연결된 label 또는 aria-label 확인
  - [ ] 키보드 탭 순서(tab order) 전 화면 점검
- [ ] **CSS 일관성 개선**
  - [ ] 하드코딩 색상 → CSS 변수 전환 (LoginView, SignupView, ProfileView 등 30~40%)
  - [ ] AppNavigation.vue: `#d4af37` → `var(--color-gold)` 통일
  - [ ] 매직 넘버 정리 (의미 있는 CSS 변수 또는 주석 추가)
- [ ] **반응형 태블릿 브레이크포인트 추가**
  - [ ] LoginView.vue: 태블릿(768px~1024px) 레이아웃 최적화
  - [ ] SignupView.vue: 태블릿 레이아웃 최적화
  - [ ] ShopView.vue: 태블릿 레이아웃 최적화
  - [ ] BattleView.vue: 태블릿 가로 모드 최적화
- [ ] **성능 최적화**
  - [ ] BattleView.vue 청크 분리 (현재 556KB — Three.js/Cannon-es 별도 vendor chunk)
  - [ ] `vite.config.ts`에 `manualChunks` 설정 (three, cannon-es, howler 분리)
  - [ ] BattleView.vue 전투 로직 composable 추출 (`useBattleLogic.ts`)
  - [ ] 이미지/아이콘 lazy loading 적용
  - [ ] 로딩 스켈레톤 UI (LoadingSpinner 대체용 SkeletonLoader 컴포넌트)
- [ ] **코드 품질 정리**
  - [ ] TODO/FIXME 주석 전수 조사 및 해결 (BattleView, SkillLoadoutView, CampaignView)
  - [ ] 한국어 코드 주석 → 영어로 통일 (SettingsView, SignupView, ProfileView 등)
  - [ ] 미사용 CSS 클래스 정리
- [ ] **사운드 에셋 적용**
  - [ ] Pixabay/Wavbvkery에서 주사위 굴림 WAV/MP3 다운로드 → `public/sounds/sfx/`
  - [ ] Silverman Sound/FMA에서 1920s 재즈 BGM 다운로드 → `public/sounds/bgm/`
  - [ ] Pixabay/ZapSplat에서 타격음 다운로드 → `public/sounds/sfx/`
  - [ ] `useSound.ts` BGM_MAP 경로와 실제 파일 매칭 확인
  - [ ] 합성음 SFX를 실제 에셋으로 교체 (옵션: 합성음 fallback 유지)
  - [ ] 라이선스 표기 파일 작성 (`public/sounds/LICENSES.md`)
- [ ] **테스트**
  - [ ] 족보 판정 단위 테스트 (evaluateHand — 프론트엔드 `battle.ts`)
  - [ ] 스킬 슬롯 검증 단위 테스트 (최대 4개, 중복 방지)
  - [ ] i18n 키 일관성 자동 검증 스크립트 (4개 파일 키 비교)
  - [ ] 크로스 브라우저 수동 테스트 (Chrome, Firefox, Safari, 모바일)

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

### 사운드 에셋 소스 (무료/로열티 프리)

**BGM (1920년대 재즈풍)**:

- [Silverman Sound "Speakeasy" (CC BY)](https://www.silvermansound.com/free-music/speakeasy)
- [Free Music Archive Jazz](https://freemusicarchive.org/genre/Jazz/)
- [Audionautix Jazz (CC BY 4.0)](https://audionautix.com/free-music/jazz)
- [Chosic Jazz](https://www.chosic.com/free-music/jazz/)

**SFX (주사위, 타격음)**:

- [Pixabay Dice SFX (무료, 저작자 표시 불필요)](https://pixabay.com/sound-effects/search/dice/)
- [Wavbvkery Dice Sounds (무료, 로열티 프리)](https://wavbvkery.com/dice-rolling-sound/)
- [Pixabay Impact SFX](https://pixabay.com/sound-effects/search/impact/)
- [ZapSplat Impacts (무료, 저작자 표시 필요)](https://www.zapsplat.com/sound-effect-category/impacts/)
- [Mixkit Impact SFX (무료)](https://mixkit.co/free-sound-effects/impact/)

### 핵심 규칙 (CLAUDE.md 참조)
- ❌ 클라이언트에서 주사위 생성 금지
- ❌ HP는 항상 100 고정
- ❌ 스킬 슬롯 최대 4개
- ❌ 동시 주사위 굴림 금지 (턴제)
- ✅ 30초 턴 타이머 필수
