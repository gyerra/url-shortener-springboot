-- PostgreSQL schema for URL Shortener
-- Hibernate ddl-auto=update will also sync; this file documents the canonical schema.

CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS urls (
    id           BIGSERIAL PRIMARY KEY,
    original_url VARCHAR(2048) NOT NULL,
    short_code   VARCHAR(12)   NOT NULL UNIQUE,
    click_count  BIGINT        NOT NULL DEFAULT 0,
    created_by   BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at   TIMESTAMPTZ,
    disabled     BOOLEAN       NOT NULL DEFAULT FALSE,
    title        VARCHAR(255),
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_urls_short_code ON urls(short_code);
CREATE INDEX IF NOT EXISTS idx_urls_created_by ON urls(created_by);

CREATE TABLE IF NOT EXISTS url_analytics (
    id          BIGSERIAL PRIMARY KEY,
    url_id      BIGINT       NOT NULL REFERENCES urls(id) ON DELETE CASCADE,
    ip_address  VARCHAR(45),
    user_agent  VARCHAR(512),
    country     VARCHAR(2),
    clicked_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_analytics_url_id ON url_analytics(url_id);
CREATE INDEX IF NOT EXISTS idx_analytics_clicked_at ON url_analytics(clicked_at);
