CREATE TABLE calendar_events (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    location    VARCHAR(200),
    starts_at   TIMESTAMPTZ  NOT NULL,
    ends_at     TIMESTAMPTZ  NOT NULL,
    CONSTRAINT ck_calendar_events_range CHECK (ends_at > starts_at)
);

CREATE INDEX idx_calendar_events_starts_at ON calendar_events (starts_at);
