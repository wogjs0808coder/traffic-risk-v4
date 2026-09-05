-- traffic-risk-v4 DB 스키마
-- 근거 문서: v4_애플리케이션설계서.md
-- 대상: PostgreSQL
-- 스키마 분리: production, stress_test (V3와 동일한 운영 방식 유지)

-- ============================================
-- 스키마 생성
-- ============================================
CREATE SCHEMA IF NOT EXISTS production;
CREATE SCHEMA IF NOT EXISTS stress_test;

-- 이후 테이블은 production 스키마 기준으로 작성.
-- stress_test 환경 구축 시 동일 DDL을 스키마명만 바꿔 재실행.

SET search_path TO production;

-- ============================================
-- user_profiles (FR-10)
-- ============================================
CREATE TABLE user_profiles (
    user_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_type VARCHAR(50) NOT NULL,
    age_group    VARCHAR(50) NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP NOT NULL DEFAULT now()
);

-- ============================================
-- prediction_history (FR-03, FR-04, FR-05 -- V3 이관)
-- ============================================
CREATE TABLE prediction_history (
    history_id      BIGSERIAL PRIMARY KEY,
    user_id         UUID REFERENCES user_profiles(user_id) ON DELETE SET NULL,
    region          VARCHAR(20) NOT NULL
                    CHECK (region IN ('seoul','busan','daegu','incheon','daejeon','yangsan')),
    input_json      JSONB NOT NULL,
    predicted_type  VARCHAR(50) NOT NULL,
    confidence      NUMERIC(5,4) NOT NULL CHECK (confidence BETWEEN 0 AND 1),
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

-- 이력 목록 조회(FR-04) 시 지역/기간 필터가 잦으므로 인덱스 추가
CREATE INDEX idx_prediction_history_region_created
    ON prediction_history (region, created_at DESC);

-- 통계 집계(FR-05) 시 사고유형 기준 GROUP BY 최적화
CREATE INDEX idx_prediction_history_predicted_type
    ON prediction_history (predicted_type);

-- ============================================
-- route_queries (FR-11~14, 신규)
-- ============================================
CREATE TABLE route_queries (
    route_id            BIGSERIAL PRIMARY KEY,
    user_id             UUID REFERENCES user_profiles(user_id) ON DELETE SET NULL,
    origin_address       VARCHAR(255) NOT NULL,
    destination_address  VARCHAR(255) NOT NULL,
    route_geojson        JSONB NOT NULL,
    segment_risk_json    JSONB NOT NULL,
    created_at           TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_route_queries_user_created
    ON route_queries (user_id, created_at DESC);

-- ============================================
-- updated_at 자동 갱신 트리거 (user_profiles)
-- ============================================
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_user_profiles_updated_at
    BEFORE UPDATE ON user_profiles
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();
