--liquibase formatted sql

--changeset inikulin:V1_create_tables
CREATE TABLE users.users (
    id UUID PRIMARY KEY,
    status VARCHAR(32) NOT NULL,
    status_timestamp TIMESTAMPTZ NOT NULL
);

CREATE TABLE users.pii (
    user_id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(255),
    CONSTRAINT fk_pii_user FOREIGN KEY (user_id) REFERENCES users.users(id) ON DELETE CASCADE
);

CREATE TABLE users.sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    created TIMESTAMPTZ NOT NULL,
    expires TIMESTAMPTZ NOT NULL,
    ip VARCHAR(45) NOT NULL,
    user_agent TEXT NOT NULL,
    CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES users.users(id) ON DELETE CASCADE
);
CREATE INDEX idx_sessions_user_id ON users.sessions(user_id);

CREATE TABLE users.consents (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    type VARCHAR(64) NOT NULL,
    accepted TIMESTAMPTZ NOT NULL,
    session UUID NOT NULL,
    CONSTRAINT fk_consent_user FOREIGN KEY (user_id) REFERENCES users.users(id) ON DELETE CASCADE,
    CONSTRAINT fk_consent_session FOREIGN KEY (session) REFERENCES users.sessions(id) ON DELETE CASCADE
);
CREATE INDEX idx_consents_user_id ON users.consents(user_id);
CREATE INDEX idx_consents_session ON users.consents(session);
