CREATE TABLE projects (
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT uq_projects_name UNIQUE (name)
);

CREATE TABLE time_entries (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    project_id  BIGINT       NOT NULL REFERENCES projects (id),
    description VARCHAR(200) NOT NULL DEFAULT '',
    started_at  TIMESTAMPTZ  NOT NULL,
    ended_at    TIMESTAMPTZ,
    CONSTRAINT ck_time_entries_range CHECK (ended_at IS NULL OR ended_at >= started_at)
);

-- At most ONE running timer (ended_at IS NULL) in the whole table: the database itself rejects a second one.
CREATE UNIQUE INDEX uq_time_entries_single_running ON time_entries ((1)) WHERE ended_at IS NULL;

CREATE INDEX idx_time_entries_started_at ON time_entries (started_at);
CREATE INDEX idx_time_entries_project_id ON time_entries (project_id);
