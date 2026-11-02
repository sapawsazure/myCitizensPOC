CREATE TABLE habits (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_habits_name UNIQUE (name)
);

CREATE TABLE habit_checkins (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    habit_id   BIGINT NOT NULL REFERENCES habits (id) ON DELETE CASCADE,
    checked_on DATE   NOT NULL,
    CONSTRAINT uq_habit_checkins_habit_day UNIQUE (habit_id, checked_on)
);
