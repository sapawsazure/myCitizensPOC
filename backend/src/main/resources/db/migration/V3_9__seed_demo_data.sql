INSERT INTO contacts (full_name, email, phone, company, notes) VALUES
    ('Asha Verma', 'asha.verma@example.com', '+91 98765 43210', 'Northwind Traders', 'Prefers email. Met at the Pune meetup.'),
    ('Marco Rossi', 'marco.rossi@example.com', '+39 06 555 0100', 'Contoso Ltd', NULL),
    ('Priya Nair', 'priya.nair@example.com', NULL, 'Fabrikam', 'Reviewer for the Phase 3 merge.'),
    ('Tom Becker', NULL, '+49 30 555 0199', NULL, 'Plumber - call after 5 pm.');

-- Times are relative to now so the current month always shows something.
INSERT INTO calendar_events (title, description, location, starts_at, ends_at) VALUES
    ('Weekly planning', 'Plan the week and review open pull requests.', 'Meeting room 2',
        date_trunc('hour', now()) - interval '2 days', date_trunc('hour', now()) - interval '2 days' + interval '1 hour'),
    ('Team stand-up', NULL, 'Online',
        date_trunc('hour', now()) + interval '1 hour', date_trunc('hour', now()) + interval '90 minutes'),
    ('Phase 3 review', 'Walk through Contacts, Calendar and Time Tracker.', 'Meeting room 1',
        date_trunc('hour', now()) + interval '1 day', date_trunc('hour', now()) + interval '1 day 1 hour'),
    ('Dinner with friends', NULL, 'Riverside Cafe',
        date_trunc('hour', now()) + interval '3 days', date_trunc('hour', now()) + interval '3 days 2 hours');

INSERT INTO projects (name) VALUES
    ('General'),
    ('Learning'),
    ('Admin');

-- Two finished sessions yesterday so the summary has something to show (no timer is left running).
INSERT INTO time_entries (project_id, description, started_at, ended_at)
SELECT p.id, v.description,
       date_trunc('day', now()) - interval '1 day' + v.start_offset,
       date_trunc('day', now()) - interval '1 day' + v.end_offset
FROM (VALUES
        ('Learning', 'Read the Flyway documentation', interval '9 hours', interval '10 hours 30 minutes'),
        ('Admin', 'Answer emails', interval '11 hours', interval '11 hours 45 minutes')
     ) AS v (project, description, start_offset, end_offset)
JOIN projects p ON p.name = v.project;
