--liquibase formatted sql

--changeset junie:V1_create_access_schema
CREATE SCHEMA IF NOT EXISTS access;

--changeset junie:V2_create_access_requests_table
CREATE TABLE access.access_requests (
    id UUID PRIMARY KEY,
    list_id UUID NOT NULL,
    requester_id UUID NOT NULL,
    owner_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_access_requests_list_id ON access.access_requests(list_id);
CREATE INDEX idx_access_requests_requester_id ON access.access_requests(requester_id);
CREATE INDEX idx_access_requests_owner_id ON access.access_requests(owner_id);
CREATE INDEX idx_access_requests_status ON access.access_requests(status);
