CREATE TABLE opinions if not exists (
    id uuid PRIMARY KEY,
    owner uuid NOT NULL,
    subject uuid NOT NULL,
    mark double precision,
    status varchar(32) NOT NULL,
    timestamp timestamp with time zone NOT NULL
);
CREATE INDEX idx_opinions_owner if not exists ON opinions(owner);
CREATE INDEX idx_opinions_subject if not exists ON opinions(subject);