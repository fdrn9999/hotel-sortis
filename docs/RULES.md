# HOTEL SORTIS Development Rules

This document contains detailed development guidelines, coding standards, and implementation patterns.

## Development Commands

### Frontend (`/frontend`)
```bash
npm install          # Install dependencies
npm run dev          # Start dev server (localhost:5173)
npm run build        # Build for production
npm run lint         # Run ESLint
npm run format       # Run Prettier
```

### Backend (`/backend`)
```bash
./gradlew bootRun    # Start server (localhost:8080)
./gradlew build      # Build project
./gradlew test       # Run tests
```

### Database Setup
```bash
mysql -u root -p -e "CREATE DATABASE hotel_sortis CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
cd docs
mysql -u root -p hotel_sortis < create.sql
mysql -u root -p hotel_sortis < insert.sql
```

## i18n Rules
- **Support**: `ko`, `en`, `ja`, `zh` (4 languages required).
- **DB**: Columns must include `_ko`, `_en`, `_ja`, `_zh`.
- **JSON**: All keys must be present in all 4 locale files.

## Architecture
- **State**: Pinia stores (`/src/stores`)
- **Routing**: Vue Router 4 (`/src/router`)
- **Structure**:
    - Frontend: Vue 3 Composition API (`<script setup>`)
    - Backend: Spring Boot 3.2 + JPA + Redis

## Core Development Rules (Strict)

### 1. Combat System
- **Turn-based**: Strictly sequential. 30s timer per turn.
- **HP**: Fixed at 100 for all entities per phase.
- **Server Authority**: Dice rolls, hand evaluation, damage calc MUST happen on server.
- **Hand Evaluation**:
    - Ace [1-1-1]: 45
    - Triple [N-N-N]: 8 + (N*4)
    - Straight [4-5-6]: 38
    - Strike [3-4-5]: 30
    - Slash [2-3-4]: 24
    - Storm [1-2-3]: 16
    - Pair [N-N-X]: 5 + (N*2)
    - NoHand: Sum of dice

### 2. Skill System
- **Slots**: Max 4 slots per player.
- **Triggers**: BATTLE_START, TURN_START, DICE_ROLL, BEFORE_DAMAGE, AFTER_DAMAGE, PASSIVE.

### 3. Monetization (Pay-to-Win Forbidden)
- No skill purchasing.
- No probability manipulation.
- No stat boosters.

### 4. UI/UX
- **Style**: 1920s Art Deco.
- **Forbidden**: `alert()`, `confirm()`, `prompt()`. Use custom Modals.
- **Navigation**: Home/Back/Settings buttons required on all screens.

### 5. Testing
- Unit tests required for Hand Evaluation (100% coverage).
- Verify HP=100 and Skill Slots=4.

See `CLAUDE.md` for high-level overview.

### 6. Check-list
- Commit-Push after Tasks
- Before Token limit reached, write summary of completed tasks and commit-push.