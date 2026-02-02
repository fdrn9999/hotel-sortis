# CLAUDE.md

## 📚 참조 문서 가이드 (Agent Context)
**클로드(에이전트)는 작업을 수행하기 전 다음 문서들을 문맥에 포함해야 합니다:**

1. **`docs/RULES.md` (필독)**
   - 상세 개발 가이드, 코드 패턴, 시스템 핵심 규칙(전투, 스킬, 상점 등)이 정의되어 있습니다.
   - **구현 전 반드시 이 파일을 확인하세요.**

2. **`TASKS.md`**
   - 현재 프로젝트의 상태, 완료된 작업, 예정된 작업이 기록되어 있습니다.
   - 작업 시작/종료 시 반드시 업데이트해야 합니다.

3. **기록 문서 (`docs/`)**
   - `docs/HISTORY.md`: 프로젝트 주요 마일스톤 및 변경 이력
   - `docs/DEVLOG_2026.md`: 일별 개발 로그 및 상세 내역
   - `docs/ARCHIVE/`: 참고용 과거 자료

---

## 프로젝트 개요
**HOTEL SORTIS**는 스킬 빌드가 조합된 턴제 전략 로그라이크 게임입니다.
- **장르**: 턴제 전략 로그라이크 / 스킬 빌드 PvP
- **플랫폼**: 웹 브라우저 (WebGL), 크로스 플랫폼 (PC/Mobile)
- **언어**: 한국어, 영어, 일본어, 중국어(간체)

## 기술 스택
- **Frontend**: Vue 3.4+, Vite 5+, Pinia, Vue Router 4, Vue I18n 9+
- **Rendering/Physics**: Three.js, Cannon-es
- **Backend**: Spring Boot 3.2+, Java 17+, Spring Data JPA
- **Database**: MariaDB 11.x, Redis 7.x
- **Communication**: REST (HATEOAS), WebSocket (STOMP)

### 실행 명령어
```bash
# Frontend
npm run dev          # Start dev server (localhost:5173)
npm run lint         # Run ESLint

# Backend
./gradlew bootRun    # Start server (localhost:8080)
./gradlew test       # Run tests
```

## 현재 작업 중인 항목 (Today's Tasks)
1. **콘텐츠 확장**: 쉴드 시스템, 층별 룰 변형(Mutators), PvP 드래프트 모드
2. **2차 폴리싱**: i18n 정합성, 하드코딩 문자열 정리, UI/UX 개선

## 꼭 필요한 규칙 (Essentials)
> **상세 규칙은 `docs/RULES.md`에 있습니다. 아래는 요약입니다.**

### 1. 전투 및 게임플레이
- **턴제 시스템**: 플레이어와 적은 번갈아가며 행동 (30초 제한). 동시 실행 금지.
- **HP 정책**: 모든 캐릭터의 HP는 페이즈당 **100 고정**.
- **서버 검증(Server Authority)**: 주사위 결과, 족보 판정, 데미지 계산은 반드시 **서버**에서 수행.
- **스킬 슬롯**: 플레이어당 **최대 4개**로 고정.

### 2. 코딩 및 스타일
- **i18n 필수**: DB(`_ko`, `_en`, `_ja`, `_zh`), JSON 파일 모두 4개 언어 지원 필수.
- **UI 스타일**: 1920년대 **Art Deco** 스타일 준수.
- **금지 API**: `alert()`, `confirm()`, `prompt()` 사용 금지. (프로젝트 내 Custom Modal 사용)