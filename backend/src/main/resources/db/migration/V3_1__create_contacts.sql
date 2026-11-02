CREATE TABLE contacts (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    full_name  VARCHAR(200) NOT NULL,
    email      VARCHAR(200),
    phone      VARCHAR(50),
    company    VARCHAR(200),
    notes      TEXT,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_contacts_full_name ON contacts (lower(full_name));
