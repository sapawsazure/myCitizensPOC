INSERT INTO tasks (title, description, status, priority, due_date) VALUES
    ('Set up Productivity Hub', 'Start the app and explore every feature card.', 'DONE', 1, CURRENT_DATE - 1),
    ('Plan the week', 'Write down the three most important goals.', 'IN_PROGRESS', 2, CURRENT_DATE + 2),
    ('Review the open pull request', NULL, 'TODO', 3, CURRENT_DATE + 1),
    ('Book the dentist', 'Call the clinic before Friday.', 'TODO', 2, CURRENT_DATE + 5);

INSERT INTO notes (title, body) VALUES
    ('Welcome', 'This note lives in PostgreSQL. Edit it or search for it with the box above.'),
    ('Meeting ideas', E'- Agree on the release date\n- Decide who reviews the merge'),
    ('Reading list', 'Spring Boot reference, Flyway docs, Pro Git (chapter on branching).');

INSERT INTO bookmarks (title, url, category) VALUES
    ('Spring Boot reference', 'https://docs.spring.io/spring-boot/index.html', 'Java'),
    ('React documentation', 'https://react.dev', 'Frontend'),
    ('PostgreSQL manual', 'https://www.postgresql.org/docs/', 'Database'),
    ('Pro Git book', 'https://git-scm.com/book', 'Git');
