--liquibase formatted sql

--changeset iatopchu:V1_create_support_messaging_tables
CREATE SCHEMA IF NOT EXISTS mock;

CREATE TABLE IF NOT EXISTS mock.support_threads (
    id UUID PRIMARY KEY,
    owner_user_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_support_threads_owner_user_id
    ON mock.support_threads(owner_user_id);
CREATE INDEX IF NOT EXISTS idx_support_threads_updated_at
    ON mock.support_threads(updated_at);

CREATE TABLE IF NOT EXISTS mock.support_messages (
    id UUID PRIMARY KEY,
    thread_id UUID NOT NULL,
    author_user_id UUID NOT NULL,
    author_role VARCHAR(32) NOT NULL,
    text TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_support_messages_thread
        FOREIGN KEY (thread_id)
        REFERENCES mock.support_threads(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_support_messages_author_role
        CHECK (author_role IN ('USER', 'SUPPORT'))
);
CREATE INDEX IF NOT EXISTS idx_support_messages_thread_id
    ON mock.support_messages(thread_id);
CREATE INDEX IF NOT EXISTS idx_support_messages_created_at
    ON mock.support_messages(created_at);