INSERT INTO expense_categories (name) VALUES
    ('Food'),
    ('Transport'),
    ('Housing'),
    ('Entertainment'),
    ('Other');

INSERT INTO expenses (category_id, description, amount, spent_on)
SELECT c.id, v.description, v.amount, CURRENT_DATE - v.days_ago
FROM (VALUES
        ('Food', 'Lunch with the team', 18.75, 0),
        ('Food', 'Groceries', 54.20, 1),
        ('Transport', 'Metro card top-up', 20.00, 2),
        ('Housing', 'Electricity bill', 87.45, 4),
        ('Entertainment', 'Movie night', 24.00, 6)
     ) AS v (category, description, amount, days_ago)
JOIN expense_categories c ON c.name = v.category;

INSERT INTO habits (name) VALUES
    ('Drink water'),
    ('Read 20 minutes'),
    ('Evening walk');

-- "Drink water": checked today and the two days before (streak 3)
INSERT INTO habit_checkins (habit_id, checked_on)
SELECT h.id, CURRENT_DATE - d.days_ago
FROM habits h
CROSS JOIN generate_series(0, 2) AS d (days_ago)
WHERE h.name = 'Drink water';

-- "Read 20 minutes": not yet today, but the three days before (streak 3 and still alive)
INSERT INTO habit_checkins (habit_id, checked_on)
SELECT h.id, CURRENT_DATE - d.days_ago
FROM habits h
CROSS JOIN generate_series(1, 3) AS d (days_ago)
WHERE h.name = 'Read 20 minutes';
