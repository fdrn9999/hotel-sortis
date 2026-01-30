# HOTEL SORTIS

> **"Alea Iacta Est. Sed Quis Iudicat?"**
> *"주사위는 던져졌다. 그러나 누가 판단하는가?"*

Turn-based Strategy Roguelike with Dice & Skill Build System

턴제 전략 로그라이크 - 주사위 & 스킬 빌드 시스템

---

## 🎮 게임 소개

HOTEL SORTIS는 **친치로(Chinchiro) 주사위 게임**을 현대적 로그라이크로 재해석한 전략 게임입니다.

### 핵심 특징
- 🎲 **3개의 주사위**로 만드는 족보 시스템
- ⚔️ **최대 4개 스킬** 조합의 전략적 깊이
- 🏨 **15층 캠페인** - 3명의 보스와 대결
- 🏆 **PvP 랭크전** - ELO 기반 매칭
- 🌍 **4개 언어 지원** - 한국어, 영어, 일본어, 중국어

---

## 🛠️ 기술 스택

### Frontend
- Vue 3.4+ (Composition API)
- Vite 5+
- Pinia (상태관리)
- Vue Router 4
- Vue I18n 9+ (다국어)
- Three.js (3D 주사위)
- Cannon-es (물리 엔진)

### Backend
- Spring Boot 3.2+
- Java 17+
- MariaDB 11.x
- Redis 7.x
- Spring Data JPA
- Spring HATEOAS
- Spring WebSocket (STOMP)

---

## 🚀 시작하기

### 요구사항
- Node.js 18+
- Java 17+
- MariaDB 11.x
- Redis 7.x

### 설치

```bash
# 저장소 클론
git clone https://github.com/your-repo/hotel-sortis.git
cd hotel-sortis

# 프론트엔드 설치
cd frontend
npm install

# 백엔드는 Gradle이 자동 처리
```

### 데이터베이스 설정

```bash
# MariaDB 접속 후 데이터베이스 생성
mysql -u root -p
CREATE DATABASE hotel_sortis CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit;

# 테이블 생성 및 초기 데이터 삽입
cd docs
mysql -u root -p hotel_sortis < create.sql   # 테이블 생성
mysql -u root -p hotel_sortis < insert.sql   # 초기 데이터 (스킬 35개, 층 15개, 보스 3개)
```

자세한 설정은 [docs/dbinit.md](docs/dbinit.md) 참조

### 실행

```bash
# 터미널 1: Backend
cd backend
./gradlew bootRun

# 터미널 2: Frontend
cd frontend
npm run dev
```

- Frontend: http://localhost:5173
- Backend: http://localhost:8080

---

## 📁 프로젝트 구조

```
hotel-sortis/
├── frontend/                # Vue 3 프론트엔드
│   ├── src/
│   │   ├── api/            # API 클라이언트
│   │   ├── views/          # 페이지 컴포넌트
│   │   ├── stores/         # Pinia 스토어
│   │   ├── router/         # 라우팅
│   │   ├── i18n/           # 다국어
│   │   ├── composables/    # 컴포저블
│   │   ├── types/          # TypeScript 타입
│   │   └── game/           # 게임 로직 (Three.js)
│   └── package.json
│
├── backend/                 # Spring Boot 백엔드
│   ├── src/main/java/com/hotelsortis/api/
│   │   ├── controller/     # REST 컨트롤러
│   │   ├── service/        # 비즈니스 로직
│   │   ├── repository/     # 데이터 접근
│   │   ├── entity/         # JPA 엔티티
│   │   ├── dto/            # 데이터 전송 객체
│   │   ├── game/           # 게임 로직
│   │   └── config/         # 설정
│   └── build.gradle
│
├── docs/                    # 문서
│   ├── dbinit.md           # DB 설정 가이드
│   ├── create.sql          # 테이블 생성 (DDL)
│   ├── insert.sql          # 초기 데이터 (DML)
│   └── script.sql          # 전체 실행 wrapper
│
├── CLAUDE.md               # AI 개발 지침서
├── PROJECTPLAN.md          # 게임 기획서
├── TASKS.md                # 작업 추적
└── README.md               # 이 파일
```

---

## 🎯 게임 규칙

### 족보 시스템

| 순위 | 족보 | 조건 | 공격력 |
|------|------|------|--------|
| 1 | 에이스 (Ace) | [1-1-1] | 180 |
| 2 | 트리플 (Triple) | [X-X-X] | X × 30 |
| 3 | 스트레이트 (Straight) | [4-5-6] | 180 |
| 4 | 스톰 (Storm) | [1-2-3] | 150 |
| 5 | 페어 (Pair) | [X-X-Y] | X × 15 |
| 6 | 노 핸드 (No Hand) | 없음 | 합계 |

### 전투 규칙
- HP는 **항상 100** 고정
- **턴제** 진행 (동시 굴림 금지)
- 턴 시간 제한: **30초**
- 최대 **10턴** (초과 시 무승부)

---

## 📖 문서

- [CLAUDE.md](CLAUDE.md) - 개발 지침서
- [PROJECTPLAN.md](PROJECTPLAN.md) - 게임 기획서
- [TASKS.md](TASKS.md) - 작업 추적
- [docs/dbinit.md](docs/dbinit.md) - 데이터베이스 설정

---

## 📜 라이선스

MIT License - [LICENSE](LICENSE) 참조

---

## 🤝 기여

이슈와 PR을 환영합니다!
