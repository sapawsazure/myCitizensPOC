import { useEffect, useState } from 'react';
import { describeError } from '../../core/http.js';
import { useResource } from '../../core/hooks.js';
import { Empty, ErrorBanner, Loading, PageHeader } from '../../core/ui.jsx';
import { timeTrackerApi } from './api.js';
import './timetracker.css';

const pad = (value) => String(value).padStart(2, '0');

/** 3725 -> "1:02:05" */
function formatDuration(totalSeconds) {
  const seconds = Math.max(0, Math.floor(totalSeconds));
  return `${Math.floor(seconds / 3600)}:${pad(Math.floor((seconds % 3600) / 60))}:${pad(seconds % 60)}`;
}

const timeText = (iso) => new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

export default function TimeTrackerPage() {
  const [projectId, setProjectId] = useState('');
  const [description, setDescription] = useState('');
  const [newProject, setNewProject] = useState('');
  const [formError, setFormError] = useState('');
  const [busy, setBusy] = useState(false);
  const [now, setNow] = useState(() => Date.now());

  const tracker = useResource(() => timeTrackerApi.state());
  const projects = useResource(() => timeTrackerApi.projects());
  const summary = useResource(() => timeTrackerApi.summary());

  const running = tracker.data?.running ?? null;
  const projectList = projects.data ?? [];
  const selectedProject = projectId || (projectList[0] ? String(projectList[0].id) : '');

  // live clock: tick once a second while a timer is running
  useEffect(() => {
    if (!running) return undefined;
    setNow(Date.now());
    const timer = setInterval(() => setNow(Date.now()), 1000);
    return () => clearInterval(timer);
  }, [running?.id]);

  const elapsedSeconds = running ? (now - new Date(running.startedAt).getTime()) / 1000 : 0;

  async function run(action) {
    setBusy(true);
    setFormError('');
    try {
      await action();
      return true;
    } catch (error) {
      setFormError(describeError(error));
      tracker.reload(); // another tab may have changed the timer
      return false;
    } finally {
      setBusy(false);
    }
  }

  async function start(event) {
    event.preventDefault();
    const started = await run(() => timeTrackerApi.start(Number(selectedProject), description));
    if (started) {
      setDescription('');
      tracker.reload();
      summary.reload();
    }
  }

  async function stop() {
    if (await run(() => timeTrackerApi.stop())) {
      tracker.reload();
      summary.reload();
    }
  }

  async function addProject(event) {
    event.preventDefault();
    if (await run(() => timeTrackerApi.addProject(newProject))) {
      setNewProject('');
      projects.reload();
    }
  }

  return (
    <section>
      <PageHeader title="Time Tracker" subtitle="Time your work with a start/stop timer per project." />

      {running ? (
        <div className="panel running-panel" aria-label="Running timer">
          <p className="muted">
            Running on <strong>{running.projectName}</strong>
            {running.description ? ` — ${running.description}` : ''}
          </p>
          <div className="clock" role="timer">
            {formatDuration(elapsedSeconds)}
          </div>
          <button type="button" className="danger" disabled={busy} onClick={stop}>
            Stop timer
          </button>
        </div>
      ) : (
        <form className="panel inline" onSubmit={start}>
          <div className="field">
            <label htmlFor="timer-project">Project</label>
            <select id="timer-project" value={selectedProject} onChange={(e) => setProjectId(e.target.value)}>
              {projectList.map((project) => (
                <option key={project.id} value={project.id}>
                  {project.name}
                </option>
              ))}
            </select>
          </div>
          <div className="field wide">
            <label htmlFor="timer-description">What are you working on?</label>
            <input
              id="timer-description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              maxLength={200}
            />
          </div>
          <button type="submit" disabled={busy || !selectedProject}>
            Start timer
          </button>
        </form>
      )}
      {formError && (
        <div className="banner error" role="alert">
          {formError}
        </div>
      )}
      <ErrorBanner error={tracker.error || projects.error || summary.error} />

      <form className="inline" onSubmit={addProject} style={{ marginBottom: 20 }}>
        <div className="field">
          <label htmlFor="new-project">New project</label>
          <input id="new-project" value={newProject} onChange={(e) => setNewProject(e.target.value)} maxLength={100} required />
        </div>
        <button type="submit" className="secondary" disabled={busy}>
          Add project
        </button>
      </form>

      {summary.data && (
        <table className="totals-table" aria-label="Time per project today">
          <caption>Worked today ({summary.data.date})</caption>
          <thead>
            <tr>
              <th scope="col">Project</th>
              <th scope="col" className="time">
                Time
              </th>
            </tr>
          </thead>
          <tbody>
            {summary.data.projects.length === 0 && (
              <tr>
                <td colSpan={2} className="muted">
                  Nothing tracked yet today.
                </td>
              </tr>
            )}
            {summary.data.projects.map((row) => (
              <tr key={row.projectId}>
                <td>{row.project}</td>
                <td className="time duration">{formatDuration(row.seconds)}</td>
              </tr>
            ))}
          </tbody>
          <tfoot>
            <tr>
              <th scope="row">Total</th>
              <td className="time duration">{formatDuration(summary.data.totalSeconds)}</td>
            </tr>
          </tfoot>
        </table>
      )}

      <h2>Today&apos;s entries</h2>
      {tracker.loading && !tracker.data && <Loading />}
      {tracker.data && tracker.data.today.length === 0 && <Empty>No entries yet today.</Empty>}
      {tracker.data && tracker.data.today.length > 0 && (
        <ul className="list">
          {tracker.data.today.map((entry) => (
            <li key={entry.id} className="item">
              <div className="grow">
                <h3>{entry.description || '(no description)'}</h3>
                <p className="muted">
                  {entry.projectName} · {timeText(entry.startedAt)} – {entry.endedAt ? timeText(entry.endedAt) : 'running'}
                </p>
              </div>
              <span className="duration">{formatDuration(entry.durationSeconds)}</span>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
