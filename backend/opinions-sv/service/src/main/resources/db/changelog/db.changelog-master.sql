--liquibase formatted sql

--changeset inikulin:V1_create_tables
CREATE SCHEMA IF NOT EXISTS opinions;

CREATE TABLE opinions.opinions (
    id UUID PRIMARY KEY,
    owner UUID NOT NULL,
    subject UUID NOT NULL,
    mark DOUBLE PRECISION,
    status VARCHAR(32) NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_opinions_owner ON opinions.opinions(owner);
CREATE INDEX idx_opinions_subject ON opinions.opinions(subject);

CREATE TABLE opinions.opinion_note (
    id UUID PRIMARY KEY,
    opinion_id UUID NOT NULL,
    type VARCHAR(32) NOT NULL,
    description TEXT NOT NULL,
    CONSTRAINT fk_opinion_note_opinion FOREIGN KEY (opinion_id) REFERENCES opinions.opinions(id) ON DELETE CASCADE
);
CREATE INDEX idx_opinion_note_opinion_id ON opinions.opinion_note(opinion_id);

CREATE TABLE opinions.weighted_opinion_reference (
    id UUID PRIMARY KEY,
    parent_opinion UUID NOT NULL,
    child_opinion UUID NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    CONSTRAINT fk_weighted_parent FOREIGN KEY (parent_opinion) REFERENCES opinions.opinions(id) ON DELETE CASCADE,
    CONSTRAINT fk_weighted_child FOREIGN KEY (child_opinion) REFERENCES opinions.opinions(id) ON DELETE CASCADE,
    CONSTRAINT chk_no_self_reference CHECK (parent_opinion <> child_opinion)
);
CREATE INDEX idx_weighted_opinion_parent ON opinions.weighted_opinion_reference(parent_opinion);
CREATE INDEX idx_weighted_opinion_child ON opinions.weighted_opinion_reference(child_opinion);

--changeset inikulin:V2_seed_data context:local,test
INSERT INTO opinions.opinions (id, owner, subject, mark, status, timestamp)
VALUES ('00000000-0000-0000-0000-000000000000'
, '07070707-0707-0707-0707-070707070707'
, '23232323-2323-2323-2323-232323232323'
, 1.07, 'DRAFT', '2024-01-01T12:00:00Z');
