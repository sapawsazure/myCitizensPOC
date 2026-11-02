CREATE TABLE bookmarks (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title      VARCHAR(200)  NOT NULL,
    url        VARCHAR(2000) NOT NULL,
    category   VARCHAR(100)  NOT NULL DEFAULT 'General',
    created_at TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_bookmarks_category ON bookmarks (category);
