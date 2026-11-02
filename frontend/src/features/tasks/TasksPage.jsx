import { useState } from 'react';
import { describeError } from '../../core/http.js';
import { useResource } from '../../core/hooks.js';
import { Empty, ErrorBanner, Loading, PageHeader } from '../../core/ui.jsx';
import { tasksApi } from './api.js';

const STATUSES = [
  ['TODO', 'To do'],
  ['IN_PROGRESS', 'In progress'],
  ['DONE', 'Done'],
];
const PRIORITIES = [
  [1, 'High'],
  [2, 'Medium'],
  [3, 'Low'],
];
const EMPTY_FORM = { title: '', priority: 2, dueDate: '' };

export default function TasksPage() {
  const [filter, setFilter] = useState('');
  const [form, setForm] = useState(EMPTY_FORM);
  const [formError, setFormError] = useState('');
  const [busy, setBusy] = useState(false);
  const tasks = useResource(() => tasksApi.list(filter), [filter]);

  async function run(action) {
    setBusy(true);
    setFormError('');
    try {
      await action();
      tasks.reload();
      return true;
    } catch (error) {
      setFormError(describeError(error));
      return false;
    } finally {
      setBusy(false);
    }
  }

  async function addTask(event) {
    event.preventDefault();
    const created = await run(() =>
      tasksApi.create({ title: form.title, priority: Number(form.priority), dueDate: form.dueDate || null }),
    );
    if (created) setForm(EMPTY_FORM);
  }

  return (
    <section>
      <PageHeader title="Tasks" subtitle="Track to-dos with status, priority and due date." />

      <form className="panel inline" onSubmit={addTask}>
        <div className="field wide">
          <label htmlFor="task-title">Title</label>
          <input
            id="task-title"
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
            maxLength={200}
            required
          />
        </div>
        <div className="field">
          <label htmlFor="task-priority">Priority</label>
          <select
            id="task-priority"
            value={form.priority}
            onChange={(e) => setForm({ ...form, priority: e.target.value })}
          >
            {PRIORITIES.map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </div>
        <div className="field">
          <label htmlFor="task-due">Due date</label>
          <input
            id="task-due"
            type="date"
            value={form.dueDate}
            onChange={(e) => setForm({ ...form, dueDate: e.target.value })}
          />
        </div>
        <button type="submit" disabled={busy}>
          Add task
        </button>
      </form>
      {formError && (
        <div className="banner error" role="alert">
          {formError}
        </div>
      )}

      <div className="tabs" role="group" aria-label="Filter by status">
        <button type="button" aria-pressed={filter === ''} onClick={() => setFilter('')}>
          All
        </button>
        {STATUSES.map(([value, label]) => (
          <button key={value} type="button" aria-pressed={filter === value} onClick={() => setFilter(value)}>
            {label}
          </button>
        ))}
      </div>

      <ErrorBanner error={tasks.error} />
      {tasks.loading && !tasks.data && <Loading />}
      {tasks.data && tasks.data.length === 0 && <Empty>No tasks here yet.</Empty>}
      {tasks.data && tasks.data.length > 0 && (
        <ul className="list">
          {tasks.data.map((task) => (
            <li key={task.id} className={`item${task.status === 'DONE' ? ' done' : ''}`}>
              <div className="grow">
                <h3>{task.title}</h3>
                <p className="muted">
                  Priority: {PRIORITIES.find(([value]) => value === task.priority)?.[1] ?? task.priority}
                  {task.dueDate && <> · Due {task.dueDate}</>}
                </p>
              </div>
              <div className="actions">
                <select
                  aria-label={`Status of ${task.title}`}
                  value={task.status}
                  disabled={busy}
                  onChange={(e) => run(() => tasksApi.setStatus(task.id, e.target.value))}
                >
                  {STATUSES.map(([value, label]) => (
                    <option key={value} value={value}>
                      {label}
                    </option>
                  ))}
                </select>
                <button
                  type="button"
                  className="danger"
                  disabled={busy}
                  aria-label={`Delete ${task.title}`}
                  onClick={() => run(() => tasksApi.remove(task.id))}
                >
                  Delete
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
