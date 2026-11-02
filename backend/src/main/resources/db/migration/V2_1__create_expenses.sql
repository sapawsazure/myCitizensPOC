CREATE TABLE expense_categories (
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT uq_expense_categories_name UNIQUE (name)
);

CREATE TABLE expenses (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    category_id BIGINT         NOT NULL REFERENCES expense_categories (id),
    description VARCHAR(200)   NOT NULL,
    amount      NUMERIC(12, 2) NOT NULL
                CONSTRAINT ck_expenses_amount CHECK (amount > 0),
    spent_on    DATE           NOT NULL,
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_expenses_spent_on ON expenses (spent_on);
CREATE INDEX idx_expenses_category_id ON expenses (category_id);
