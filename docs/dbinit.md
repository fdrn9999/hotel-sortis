# Database Initialization Guide
# 데이터베이스 초기화 가이드

## 요구사항

- **MariaDB 11.x** 이상
- **Redis 7.x** 이상

---

## 1. MariaDB 설치 및 설정

### Windows
```bash
# Chocolatey 사용
choco install mariadb

# 또는 공식 사이트에서 다운로드
# https://mariadb.org/download/
```

### macOS
```bash
brew install mariadb
brew services start mariadb
```

### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install mariadb-server
sudo systemctl start mariadb
sudo systemctl enable mariadb
```

---

## 2. 데이터베이스 생성

### MariaDB 접속
```bash
mysql -u root -p
```

### 데이터베이스 및 사용자 생성
```sql
-- 데이터베이스 생성
CREATE DATABASE hotel_sortis CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 사용자 생성 (개발용)
CREATE USER 'hotelsortis'@'localhost' IDENTIFIED BY 'your_password';

-- 권한 부여
GRANT ALL PRIVILEGES ON hotel_sortis.* TO 'hotelsortis'@'localhost';
FLUSH PRIVILEGES;

-- 확인
SHOW DATABASES;
```

---

## 3. 테이블 초기화

### SQL 파일 구조

```
docs/
├── create.sql   # 테이블 생성 (DDL)
├── insert.sql   # 초기 데이터 삽입 (DML) - 스킬 60개, 층 15개, 보스 3개
└── script.sql   # 전체 실행 (create + insert)
```

### 방법 1: JPA 자동 생성 (개발용)
`application.yml`에서 `ddl-auto: update` 설정 시 테이블 자동 생성됨
(초기 데이터는 별도로 insert.sql 실행 필요)

### 방법 2: SQL 스크립트 개별 실행 (권장)
```bash
cd docs

# 1. 테이블 생성
mysql -u hotelsortis -p hotel_sortis < create.sql

# 2. 초기 데이터 삽입
mysql -u hotelsortis -p hotel_sortis < insert.sql
```

### 방법 3: 전체 스크립트 실행
```bash
cd docs
mysql -u hotelsortis -p hotel_sortis < script.sql
```

### 초기 데이터 내용

| 항목 | 개수 | 설명 |
|------|------|------|
| **스킬** | 60개 | Common 10, Rare 15, Epic 20, Legendary 15 |
| **층** | 15개 | 일반 8, 엘리트 4, 보스 3 |
| **보스** | 3개 | Mammon(5층), Eligor(10층), Lucifuge(15층) |
| **시즌** | 1개 | Season 1 (2026 Q1) |
| **테스트 계정** | 2개 | testplayer, testplayer2 |

---

## 4. Redis 설치 및 설정

### Windows
```bash
# WSL2 사용 권장
wsl --install
# WSL 내에서:
sudo apt install redis-server
sudo service redis-server start
```

### macOS
```bash
brew install redis
brew services start redis
```

### Linux
```bash
sudo apt install redis-server
sudo systemctl start redis-server
sudo systemctl enable redis-server
```

### 연결 테스트
```bash
redis-cli ping
# 응답: PONG
```

---

## 5. 환경 변수 설정

### 방법 1: 환경 변수
```bash
export DB_USERNAME=hotelsortis
export DB_PASSWORD=1q2w3e4r!
export REDIS_HOST=localhost
export REDIS_PORT=6379
```

### 방법 2: application-local.yml (Git 제외됨)
```yaml
spring:
  datasource:
    username: hotelsortis
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
```

---

## 6. 연결 확인

### Backend 실행
```bash
cd backend
./gradlew bootRun
```

### 정상 연결 시 로그
```
HikariPool-1 - Starting...
HikariPool-1 - Added connection org.mariadb.jdbc.Connection@xxxxx
```

---

## 트러블슈팅

### MariaDB 연결 실패
```bash
# 서비스 상태 확인
sudo systemctl status mariadb

# 포트 확인
netstat -tlnp | grep 3306
```

### Redis 연결 실패
```bash
# 서비스 상태 확인
sudo systemctl status redis-server

# 포트 확인
netstat -tlnp | grep 6379
```

### 권한 문제
```sql
-- 권한 재설정
GRANT ALL PRIVILEGES ON hotel_sortis.* TO 'hotelsortis'@'localhost';
FLUSH PRIVILEGES;
```
