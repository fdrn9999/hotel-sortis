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
- [x] insert.sql 초기 데이터 스크립트 (DML) - 스킬 35개, 층 15개, 보스 3개
- [x] TASKS.md 작업 추적 문서
- [x] README.md 업데이트
- [x] CLAUDE.md 개발 지침서 업데이트

---

## 🔄 진행 중 (In Progress)

_현재 진행 중인 작업 없음_

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
