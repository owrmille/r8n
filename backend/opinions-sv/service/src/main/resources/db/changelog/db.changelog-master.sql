--liquibase formatted sql

--changeset inikulin:V1_create_tables
CREATE SCHEMA IF NOT EXISTS opinions;

CREATE TABLE IF NOT EXISTS opinions.opinions (
    id UUID PRIMARY KEY,
    owner UUID NOT NULL,
    subject UUID NOT NULL,
    mark DOUBLE PRECISION,
    status VARCHAR(32) NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_opinions_owner ON opinions.opinions(owner);
CREATE INDEX IF NOT EXISTS idx_opinions_subject ON opinions.opinions(subject);

CREATE TABLE IF NOT EXISTS opinions.opinion_notes (
    id UUID PRIMARY KEY,
    opinion_id UUID NOT NULL,
    type VARCHAR(32) NOT NULL,
    description TEXT NOT NULL,
    CONSTRAINT fk_opinion_note_opinion FOREIGN KEY (opinion_id) REFERENCES opinions.opinions(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_opinion_note_opinion_id ON opinions.opinion_notes(opinion_id);

CREATE TABLE IF NOT EXISTS opinions.weighted_opinion_references (
    id UUID PRIMARY KEY,
    parent_opinion UUID NOT NULL,
    child_opinion UUID NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    CONSTRAINT fk_weighted_parent FOREIGN KEY (parent_opinion) REFERENCES opinions.opinions(id) ON DELETE CASCADE,
    CONSTRAINT fk_weighted_child FOREIGN KEY (child_opinion) REFERENCES opinions.opinions(id) ON DELETE CASCADE,
    CONSTRAINT chk_no_self_reference CHECK (parent_opinion <> child_opinion)
);
CREATE INDEX IF NOT EXISTS idx_weighted_opinion_parent ON opinions.weighted_opinion_references(parent_opinion);
CREATE INDEX IF NOT EXISTS idx_weighted_opinion_child ON opinions.weighted_opinion_references(child_opinion);

--changeset iatopchu:V2_seed_bernard_on_cap1 context:local,test
INSERT INTO opinions.opinions (id, owner, subject, mark, status, timestamp)
VALUES ('30000000-0000-0000-0000-000000000001'
, '10101010-1010-1010-1010-101010101010'
, '14141414-1414-1414-1414-141414141414'
, 4.23, 'DRAFT', '2024-02-01T09:30:00Z');

INSERT INTO opinions.opinion_notes (id, opinion_id, type, description)
VALUES ('30000000-0000-0000-0000-000000000101', '30000000-0000-0000-0000-000000000001', 'SUBJECTIVE', 'reminds of grandma''s home coffee');

INSERT INTO opinions.opinion_notes (id, opinion_id, type, description)
VALUES ('30000000-0000-0000-0000-000000000102', '30000000-0000-0000-0000-000000000001', 'OBJECTIVE', '5.50€');

INSERT INTO opinions.opinion_notes (id, opinion_id, type, description)
VALUES ('30000000-0000-0000-0000-000000000103', '30000000-0000-0000-0000-000000000001', 'OBJECTIVE', 'lactose-free milk');

--changeset iatopchu:V3_subjects_and_referents
CREATE TABLE IF NOT EXISTS opinions.referents (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    referent_group UUID NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_referents_referent_group
    ON opinions.referents(referent_group);
CREATE INDEX IF NOT EXISTS idx_referents_name
    ON opinions.referents(name);

CREATE TABLE IF NOT EXISTS opinions.subjects (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    referent UUID NOT NULL,
    CONSTRAINT fk_subjects_referent
    FOREIGN KEY (referent)
    REFERENCES referents(id)
);
CREATE INDEX IF NOT EXISTS idx_subjects_referent
    ON opinions.subjects(referent);
CREATE INDEX IF NOT EXISTS idx_subjects_name
    ON opinions.subjects(name);

--changeset iatopchu:V3_seed_subjects_referents context:local,test
INSERT INTO opinions.referents (id, name, address, latitude, longitude, referent_group)
VALUES
    ('12121212-1212-1212-1212-121212121212', 'cappuccino @ Cafe Eins A', 'Berlin, Alexanderplatz 1', 52.5217457, 13.4097131, '41414141-4141-4141-4141-414141414141'),
    ('13131313-1313-1313-1313-131313131313', 'cappuccino @ Cafe Eins G', 'Berlin, Görlitzer str 1', 52.49921333621459, 13.432348384513238, '41414141-4141-4141-4141-414141414141')
ON CONFLICT (id) DO NOTHING;

INSERT INTO opinions.subjects (id, name, referent)
VALUES
    ('14141414-1414-1414-1414-141414141414', 'cappuccino @ Cafe Eins A', '12121212-1212-1212-1212-121212121212'),
    ('15151515-1515-1515-1515-151515151515', 'cappuccino @ Cafe Eins G', '13131313-1313-1313-1313-131313131313')
ON CONFLICT (id) DO NOTHING;

--changeset iatopchu:V4_unique_component_link_pair
CREATE UNIQUE INDEX IF NOT EXISTS uq_weighted_opinion_parent_child
    ON opinions.weighted_opinion_references(parent_opinion, child_opinion);

--changeset inikulin:V5_seed_test_user context:local,test
INSERT INTO opinions.opinions (id, owner, subject, mark, status, timestamp)
VALUES ('30000000-0000-0000-0000-000000000002'
       , '00000000-0000-0000-0000-000000000000'
       , '14141414-1414-1414-1414-141414141414'
       , 4.24, 'DRAFT', '2024-02-01T09:30:00Z');

--changeset inikulin:V6_access
CREATE TABLE opinions.access_requests (
    id UUID PRIMARY KEY,
    list UUID NOT NULL,
    requester UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_access_requests_id ON opinions.access_requests(id);
CREATE INDEX idx_access_requests_requester ON opinions.access_requests(requester);
CREATE INDEX idx_access_requests_status ON opinions.access_requests(status);

--changeset inikulin:V7_opinion_lists
CREATE TABLE opinions.opinion_lists (
    id UUID PRIMARY KEY,
    owner UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    privacy VARCHAR(32) NOT NULL
);

CREATE INDEX idx_opinion_lists_owner ON opinions.opinion_lists(owner);

CREATE TABLE opinions.opinions_to_lists (
    id UUID PRIMARY KEY,
    opinion_list UUID NOT NULL,
    opinion UUID NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    CONSTRAINT fk_opinions_to_lists_list FOREIGN KEY (opinion_list) REFERENCES opinions.opinion_lists(id) ON DELETE CASCADE,
    CONSTRAINT fk_opinions_to_lists_opinion FOREIGN KEY (opinion) REFERENCES opinions.opinions(id) ON DELETE CASCADE
);

CREATE INDEX idx_opinions_to_lists_list ON opinions.opinions_to_lists(opinion_list);
CREATE INDEX idx_opinions_to_lists_opinion ON opinions.opinions_to_lists(opinion);

--changeset inikulin:V8_seed_opinion_lists context:local,test
INSERT INTO opinions.opinion_lists (id, owner, name, privacy)
VALUES ('70000000-0000-0000-0000-000000000001', '10101010-1010-1010-1010-101010101010', 'Bernard''s cappuccino rating', 'SEARCHABLE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO opinions.opinions (id, owner, subject, mark, status, timestamp)
VALUES ('30000000-0000-0000-0000-000000000003', '10101010-1010-1010-1010-101010101010', '21212121-2121-2121-2121-212121212121', 4.90, 'PUBLISHED', '2024-02-01T10:30:00Z')
ON CONFLICT (id) DO NOTHING;

INSERT INTO opinions.opinion_notes (id, opinion_id, type, description)
VALUES ('30000000-0000-0000-0000-000000000104', '30000000-0000-0000-0000-000000000003', 'SUBJECTIVE', 'my favorite')
ON CONFLICT (id) DO NOTHING;

INSERT INTO opinions.opinion_notes (id, opinion_id, type, description)
VALUES ('30000000-0000-0000-0000-000000000105', '30000000-0000-0000-0000-000000000003', 'OBJECTIVE', '5.75€')
ON CONFLICT (id) DO NOTHING;

INSERT INTO opinions.opinions_to_lists (id, opinion_list, opinion, weight)
VALUES 
    ('30000000-0000-0000-0000-000000000001', '70000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 1.0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO OPINIONS.ACCESS_REQUESTS(id, list, requester, status, created_at, updated_at)
VALUES (
         '30000000-0000-0000-0000-100000000001'
       , '70000000-0000-0000-0000-000000000001'
       , '09090909-0909-0909-0909-090909090909'
       , 'ACCEPTED'
       , '2024-02-01T09:30:00Z'
       , '2024-02-01T09:30:00Z'
       )
    ON CONFLICT (id) DO NOTHING;

INSERT INTO OPINIONS.ACCESS_REQUESTS(id, list, requester, status, created_at, updated_at)
VALUES (
 '30000000-0000-0000-0000-200000000001'
, '70000000-0000-0000-0000-000000000001'
, '00000000-0000-0000-0000-000000000000'
, 'ACCEPTED'
, '2024-02-01T09:30:00Z'
, '2024-02-01T09:30:00Z'
)
ON CONFLICT (id) DO NOTHING;

--changeset ditabisko:V9_seed_users_opinions_and_requests context:local,test

-- New referents (Berlin coffee spots)
INSERT INTO opinions.referents (id, name, address, latitude, longitude, referent_group)
VALUES
    ('16161616-1616-1616-1616-161616161616', 'espresso @ The Barn', 'Berlin, Auguststraße 58', 52.5264, 13.3982, '41414141-4141-4141-4141-414141414141'),
    ('17171717-1717-1717-1717-171717171717', 'flat white @ Five Elephant', 'Berlin, Reichenberger Str. 101', 52.4942, 13.4246, '41414141-4141-4141-4141-414141414141'),
    ('18181818-1818-1818-1818-181818181818', 'latte @ Father Carpenter', 'Berlin, Münzstraße 21', 52.5253, 13.4072, '41414141-4141-4141-4141-414141414141')
ON CONFLICT (id) DO NOTHING;

-- New subjects
INSERT INTO opinions.subjects (id, name, referent)
VALUES
    ('1a1a1a1a-1a1a-1a1a-1a1a-1a1a1a1a1a1a', 'espresso @ The Barn', '16161616-1616-1616-1616-161616161616'),
    ('1b1b1b1b-1b1b-1b1b-1b1b-1b1b1b1b1b1b', 'flat white @ Five Elephant', '17171717-1717-1717-1717-171717171717'),
    ('1c1c1c1c-1c1c-1c1c-1c1c-1c1c1c1c1c1c', 'latte @ Father Carpenter', '18181818-1818-1818-1818-181818181818')
ON CONFLICT (id) DO NOTHING;

-- Opinions for Anna, Lena, Max, Sofia, Jonas + Test Testsson's second opinion
INSERT INTO opinions.opinions (id, owner, subject, mark, status, timestamp)
VALUES
    ('30000000-0000-0000-0000-000000000004', '20202020-2020-2020-2020-202020202020', '1a1a1a1a-1a1a-1a1a-1a1a-1a1a1a1a1a1a', 8.5, 'PUBLISHED', '2024-03-01T09:00:00Z'),
    ('30000000-0000-0000-0000-000000000005', '30303030-3030-3030-3030-303030303030', '1b1b1b1b-1b1b-1b1b-1b1b-1b1b1b1b1b1b', 7.0, 'PUBLISHED', '2024-03-02T10:00:00Z'),
    ('30000000-0000-0000-0000-000000000006', '40404040-4040-4040-4040-404040404040', '1c1c1c1c-1c1c-1c1c-1c1c-1c1c1c1c1c1c', 6.5, 'PUBLISHED', '2024-03-03T11:00:00Z'),
    ('30000000-0000-0000-0000-000000000007', '50505050-5050-5050-5050-505050505050', '1a1a1a1a-1a1a-1a1a-1a1a-1a1a1a1a1a1a', 9.0, 'PUBLISHED', '2024-03-04T12:00:00Z'),
    ('30000000-0000-0000-0000-000000000008', '60606060-6060-6060-6060-606060606060', '1b1b1b1b-1b1b-1b1b-1b1b-1b1b1b1b1b1b', 7.5, 'PUBLISHED', '2024-03-05T13:00:00Z'),
    ('30000000-0000-0000-0000-000000000009', '00000000-0000-0000-0000-000000000000', '1c1c1c1c-1c1c-1c1c-1c1c-1c1c1c1c1c1c', 8.0, 'PUBLISHED', '2024-03-06T14:00:00Z')
ON CONFLICT (id) DO NOTHING;

-- Opinion notes
INSERT INTO opinions.opinion_notes (id, opinion_id, type, description)
VALUES
    ('30000000-0000-0000-0000-000000000201', '30000000-0000-0000-0000-000000000004', 'SUBJECTIVE', 'clean and bright, very precise'),
    ('30000000-0000-0000-0000-000000000202', '30000000-0000-0000-0000-000000000004', 'OBJECTIVE', '3.80€'),
    ('30000000-0000-0000-0000-000000000203', '30000000-0000-0000-0000-000000000005', 'SUBJECTIVE', 'silky texture, great balance'),
    ('30000000-0000-0000-0000-000000000204', '30000000-0000-0000-0000-000000000005', 'OBJECTIVE', '4.50€'),
    ('30000000-0000-0000-0000-000000000205', '30000000-0000-0000-0000-000000000006', 'SUBJECTIVE', 'a bit too milky for my taste'),
    ('30000000-0000-0000-0000-000000000206', '30000000-0000-0000-0000-000000000006', 'OBJECTIVE', '4.20€'),
    ('30000000-0000-0000-0000-000000000207', '30000000-0000-0000-0000-000000000007', 'SUBJECTIVE', 'best espresso in Berlin'),
    ('30000000-0000-0000-0000-000000000208', '30000000-0000-0000-0000-000000000007', 'OBJECTIVE', '3.80€'),
    ('30000000-0000-0000-0000-000000000209', '30000000-0000-0000-0000-000000000008', 'SUBJECTIVE', 'consistent and reliable'),
    ('30000000-0000-0000-0000-000000000210', '30000000-0000-0000-0000-000000000008', 'OBJECTIVE', '4.50€'),
    ('30000000-0000-0000-0000-000000000211', '30000000-0000-0000-0000-000000000009', 'SUBJECTIVE', 'smooth and well-rounded'),
    ('30000000-0000-0000-0000-000000000212', '30000000-0000-0000-0000-000000000009', 'OBJECTIVE', '4.20€')
ON CONFLICT (id) DO NOTHING;

-- Test Testsson's RESTRICTED list
INSERT INTO opinions.opinion_lists (id, owner, name, privacy)
VALUES ('70000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000000', 'My Berlin coffee picks', 'PRIVATE')
ON CONFLICT (id) DO NOTHING;

-- Anna's SEARCHABLE list
INSERT INTO opinions.opinion_lists (id, owner, name, privacy)
VALUES ('70000000-0000-0000-0000-000000000003', '20202020-2020-2020-2020-202020202020', 'Anna''s espresso tour', 'SEARCHABLE')
ON CONFLICT (id) DO NOTHING;

-- Lena's SEARCHABLE list
INSERT INTO opinions.opinion_lists (id, owner, name, privacy)
VALUES ('70000000-0000-0000-0000-000000000004', '30303030-3030-3030-3030-303030303030', 'Lena''s flat white spots', 'SEARCHABLE')
ON CONFLICT (id) DO NOTHING;

-- Opinions linked to lists
INSERT INTO opinions.opinions_to_lists (id, opinion_list, opinion, weight)
VALUES
    ('30000000-0000-0000-0000-000000000010', '70000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000009', 1.0),
    ('30000000-0000-0000-0000-000000000011', '70000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000004', 1.0),
    ('30000000-0000-0000-0000-000000000012', '70000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000005', 1.0)
ON CONFLICT (id) DO NOTHING;

-- Access requests to Test Testsson's restricted list
-- Anna, Lena, Max, Sofia → PENDING (will show in badge)
-- Jonas → ACCEPTED (for variety)
INSERT INTO opinions.access_requests (id, list, requester, status, created_at, updated_at)
VALUES
    ('30000000-0000-0000-0000-300000000001', '70000000-0000-0000-0000-000000000002', '20202020-2020-2020-2020-202020202020', 'SENT', '2026-04-11T09:00:00Z', '2026-04-11T09:00:00Z'),
    ('30000000-0000-0000-0000-300000000002', '70000000-0000-0000-0000-000000000002', '30303030-3030-3030-3030-303030303030', 'SENT', '2026-04-14T10:00:00Z', '2026-04-14T10:00:00Z'),
    ('30000000-0000-0000-0000-300000000003', '70000000-0000-0000-0000-000000000002', '40404040-4040-4040-4040-404040404040', 'SENT', '2026-04-18T11:00:00Z', '2026-04-18T11:00:00Z'),
    ('30000000-0000-0000-0000-300000000004', '70000000-0000-0000-0000-000000000002', '50505050-5050-5050-5050-505050505050', 'SENT', '2026-04-22T12:00:00Z', '2026-04-22T12:00:00Z'),
    ('30000000-0000-0000-0000-300000000005', '70000000-0000-0000-0000-000000000002', '60606060-6060-6060-6060-606060606060', 'ACCEPTED', '2026-04-23T13:00:00Z', '2026-04-23T14:00:00Z')
ON CONFLICT (id) DO NOTHING;