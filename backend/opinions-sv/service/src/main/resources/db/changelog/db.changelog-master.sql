--liquibase formatted sql

--changeset inikulin:V1_create_schema
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

CREATE TABLE IF NOT EXISTS opinion_note (
    id UUID PRIMARY KEY,
    opinion_id UUID NOT NULL,
    type VARCHAR(32) NOT NULL,
    description TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_opinion_note_opinion_id
    ON opinion_note(opinion_id);
ALTER TABLE opinion_note
    ADD CONSTRAINT fk_opinion_note_opinion
    FOREIGN KEY (opinion_id)
    REFERENCES opinions(id)
    ON DELETE CASCADE;

CREATE TABLE IF NOT EXISTS weighted_opinion_reference(
    id UUID PRIMARY KEY,
    parent_opinion UUID NOT NULL,
    child_opinion UUID NOT NULL,
    weight DOUBLE PRECISION NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_weighted_opinion_parent
    ON weighted_opinion_reference(parent_opinion);
CREATE INDEX IF NOT EXISTS idx_weighted_opinion_child
    ON weighted_opinion_reference(child_opinion);
ALTER TABLE weighted_opinion_reference
    ADD CONSTRAINT fk_weighted_parent
    FOREIGN KEY (parent_opinion)
    REFERENCES opinions(id);
ALTER TABLE weighted_opinion_reference
    ADD CONSTRAINT fk_weighted_child
    FOREIGN KEY (child_opinion)
    REFERENCES opinions(id);
ALTER TABLE weighted_opinion_reference
    ADD CONSTRAINT chk_no_self_reference
    CHECK (parent_opinion <> child_opinion);

--changeset inikulin:V2_seed_data context:local,test
INSERT INTO opinions (id, owner, subject, mark, status, timestamp)
VALUES ('00000000-0000-0000-0000-000000000000'
, '11111111-1111-1111-1111-111111111111'
, '22222222-2222-2222-2222-222222222222'
, 5.0, 'DRAFT', NOW());
