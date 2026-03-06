--liquibase formatted sql

-- Changeset: schema creation (always)
--changeset author:inikulin id:V1__create_schema
CREATE TABLE IF NOT EXISTS opinions (
    id UUID PRIMARY KEY,
    owner UUID NOT NULL,
    subject UUID NOT NULL,
    mark DOUBLE PRECISION,
    status VARCHAR(32) NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_opinions_owner ON opinions(owner);
CREATE INDEX IF NOT EXISTS idx_opinions_subject ON opinions(subject);

-- Changeset: seed data (только dev/test)
--changeset author:inikulin id:V2__seed_data context:local,test
INSERT INTO opinions (id, owner, subject, mark, status, timestamp)
VALUES ('00000000-0000-0000-0000-000000000000'
, '11111111-1111-1111-1111-111111111111'
, '22222222-2222-2222-2222-222222222222'
, 5.0, 'DRAFT', NOW());
