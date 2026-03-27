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

--changeset iatopchu:V2_seed_bernard_on_cap1 context:local,test
INSERT INTO opinions (id, owner, subject, mark, status, timestamp)
VALUES ('30000000-0000-0000-0000-000000000001'
, '10101010-1010-1010-1010-101010101010'
, '14141414-1414-1414-1414-141414141414'
, 4.23, 'DRAFT', '2024-02-01T09:30:00Z');

INSERT INTO opinion_note (id, opinion_id, type, description)
VALUES ('30000000-0000-0000-0000-000000000101', '30000000-0000-0000-0000-000000000001', 'SUBJECTIVE', 'reminds of grandma''s home coffee');

INSERT INTO opinion_note (id, opinion_id, type, description)
VALUES ('30000000-0000-0000-0000-000000000102', '30000000-0000-0000-0000-000000000001', 'OBJECTIVE', '5.50€');

INSERT INTO opinion_note (id, opinion_id, type, description)
VALUES ('30000000-0000-0000-0000-000000000103', '30000000-0000-0000-0000-000000000001', 'OBJECTIVE', 'lactose-free milk');

--changeset iatopchu:V3_subjects_and_referents context:local,test
CREATE TABLE IF NOT EXISTS referents (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    referent_group UUID NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_referents_referent_group
    ON referents(referent_group);
CREATE INDEX IF NOT EXISTS idx_referents_name
    ON referents(name);

CREATE TABLE IF NOT EXISTS subjects (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    referent UUID NOT NULL,
    CONSTRAINT fk_subjects_referent
    FOREIGN KEY (referent)
    REFERENCES referents(id)
);
CREATE INDEX IF NOT EXISTS idx_subjects_referent
    ON subjects(referent);
CREATE INDEX IF NOT EXISTS idx_subjects_name
    ON subjects(name);

INSERT INTO referents (id, name, address, latitude, longitude, referent_group)
VALUES
    ('12121212-1212-1212-1212-121212121212', 'cappuccino @ Cafe Eins A', 'Berlin, Alexanderplatz 1', 52.5217457, 13.4097131, '41414141-4141-4141-4141-414141414141'),
    ('13131313-1313-1313-1313-131313131313', 'cappuccino @ Cafe Eins G', 'Berlin, Görlitzer str 1', 52.49921333621459, 13.432348384513238, '41414141-4141-4141-4141-414141414141')
ON CONFLICT (id) DO NOTHING;

INSERT INTO subjects (id, name, referent)
VALUES
    ('14141414-1414-1414-1414-141414141414', 'cappuccino @ Cafe Eins A', '12121212-1212-1212-1212-121212121212'),
    ('15151515-1515-1515-1515-151515151515', 'cappuccino @ Cafe Eins G', '13131313-1313-1313-1313-131313131313')
ON CONFLICT (id) DO NOTHING;
