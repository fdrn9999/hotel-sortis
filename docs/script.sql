-- =====================================================
-- HOTEL SORTIS - Full Database Initialization Script
-- 호텔 소르티스 - 전체 데이터베이스 초기화 스크립트
-- =====================================================
--
-- 이 파일은 create.sql과 insert.sql을 순서대로 실행합니다.
--
-- 실행 방법 1 (전체 초기화):
--   mysql -u root -p hotel_sortis < script.sql
--
-- 실행 방법 2 (개별 실행):
--   mysql -u root -p hotel_sortis < create.sql
--   mysql -u root -p hotel_sortis < insert.sql
--
-- 파일 구조:
--   create.sql - 테이블 생성 (DDL)
--   insert.sql - 초기 데이터 삽입 (DML)
--
-- =====================================================

-- 데이터베이스 생성 (필요시 주석 해제)
-- CREATE DATABASE IF NOT EXISTS hotel_sortis CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE hotel_sortis;

-- =====================================================
-- 1. 테이블 생성 (create.sql 내용)
-- =====================================================
SOURCE create.sql;

-- =====================================================
-- 2. 초기 데이터 삽입 (insert.sql 내용)
-- =====================================================
SOURCE insert.sql;

-- =====================================================
-- 완료 메시지
-- =====================================================
SELECT '=====================================' AS '';
SELECT 'Database initialization complete!' AS message;
SELECT '=====================================' AS '';
