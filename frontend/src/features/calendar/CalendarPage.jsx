import { useState } from 'react';
import { describeError } from '../../core/http.js';
import { useResource } from '../../core/hooks.js';
import { Empty, ErrorBanner, Loading, PageHeader } from '../../core/ui.jsx';
import { calendarApi } from './api.js';
import './calendar.css';

const pad = (value) => String(value).padStart(2, '0');

const monthOf = (date) => `${date.getFullYear()}-${pad(date.getMonth() + 1)}`;
const dayKeyOf = (date) => `${monthOf(date)}-${pad(date.getDate())}`;

function shiftMonth(month, delta) {
  const [year, number] = month.split('-').map(Number);
  return monthOf(new Date(year, number - 1 + delta, 1));
}

/** Date -> value for <input type="datetime-local"> in the browser's own time zone. */
const toLocalInput = (date) => `${dayKeyOf(date)}T${pad(date.getHours())}:${pad(date.getMinutes())}`;

function emptyForm() {
  const start = new Date();
  start.setHours(start.getHours() + 1, 0, 0, 0);
  const end = new Date(start.getTime() + 60 * 60 * 1000);
  return { title: '', location: '', description: '', startsAt: toLocalInput(start), endsAt: toLocalInput(end) };
}

const timeText = (iso) => new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

function dayHeading(key) {
  return new Date(`${key}T12:00:00`).toLocaleDateString(undefined, { weekday: 'long', day: 'numeric', month: 'long' });
}

/** Groups events (already sorted by start) by the day they start on. */
function groupByDay(events) {
  const groups = [];
  events.forEach((event) => {
    const key = dayKeyOf(new Date(event.startsAt));
    const last = groups[groups.length - 1];
    if (last && last.key === key) last.events.push(event);
    else groups.push({ key, events: [event] });
  });
  return groups;
}

export default function CalendarPage() {
  const [month, setMonth] = useState(monthOf(new Date()));
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [formError, setFormError] = useState('');
  const [busy, setBusy] = useState(false);
  const events = useResource(() => (month ? calendarApi.list(month) : Promise.resolve([])), [month]);

  const setField = (field) => (event) => setForm({ ...form, [field]: event.target.value });

  async function run(action) {
    setBusy(true);
    setFormError('');
    try {
      await action();
      return true;
    } catch (error) {
      setFormError(describeError(error));
      return false;
    } finally {
      setBusy(false);
    }
  }

  function startEdit(event) {
    setEditingId(event.id);
    setForm({
      title: event.title,
      location: event.location ?? '',
      description: event.description ?? '',
      startsAt: toLocalInput(new Date(event.startsAt)),
      endsAt: toLocalInput(new Date(event.endsAt)),
    });
  }

  function reset() {
    setEditingId(null);
    setForm(emptyForm());
  }

  async function save(submitEvent) {
    submitEvent.preventDefault();
    const payload = {
      title: form.title,
      description: form.description,
      location: form.location,
      startsAt: new Date(form.startsAt).toISOString(),
      endsAt: new Date(form.endsAt).toISOString(),
    };
    const saved = await run(() =>
      editingId ? calendarApi.update(editingId, payload) : calendarApi.create(payload),
    );
    if (!saved) return;
    const savedMonth = monthOf(new Date(form.startsAt));
    reset();
    if (savedMonth === month) events.reload();
    else setMonth(savedMonth); // jump to the month that now contains the event
  }

  async function removeEvent(id) {
    if (await run(() => calendarApi.remove(id))) events.reload();
  }

  return (
    <section>
      <PageHeader title="Calendar" subtitle="Plan events and browse them by month." />

      <form className="panel inline" onSubmit={save}>
        <div className="field wide">
          <label htmlFor="event-title">Title</label>
          <input id="event-title" value={form.title} onChange={setField('title')} maxLength={200} required />
        </div>
        <div className="field">
          <label htmlFor="event-start">Starts</label>
          <input id="event-start" type="datetime-local" value={form.startsAt} onChange={setField('startsAt')} required />
        </div>
        <div className="field">
          <label htmlFor="event-end">Ends</label>
          <input id="event-end" type="datetime-local" value={form.endsAt} onChange={setField('endsAt')} required />
        </div>
        <div className="field">
          <label htmlFor="event-location">Location</label>
          <input id="event-location" value={form.location} onChange={setField('location')} maxLength={200} />
        </div>
        <div className="field wide">
          <label htmlFor="event-description">Description</label>
          <input id="event-description" value={form.description} onChange={setField('description')} maxLength={5000} />
        </div>
        <div>
          <button type="submit" disabled={busy}>
            {editingId ? 'Save event' : 'Add event'}
          </button>{' '}
          {editingId && (
            <button type="button" className="secondary" onClick={reset}>
              Cancel
            </button>
          )}
        </div>
      </form>
      {formError && (
        <div className="banner error" role="alert">
          {formError}
        </div>
      )}

      <div className="month-nav">
        <button type="button" className="secondary" onClick={() => setMonth(shiftMonth(month, -1))}>
          Previous month
        </button>
        <div className="field">
          <label htmlFor="calendar-month">Month</label>
          <input id="calendar-month" type="month" value={month} onChange={(e) => setMonth(e.target.value)} />
        </div>
        <button type="button" className="secondary" onClick={() => setMonth(shiftMonth(month, 1))}>
          Next month
        </button>
      </div>

      <ErrorBanner error={events.error} />
      {events.loading && !events.data && <Loading />}
      {events.data && events.data.length === 0 && <Empty>No events this month.</Empty>}
      {events.data &&
        groupByDay(events.data).map((group) => (
          <div key={group.key} className="day-group">
            <h2>{dayHeading(group.key)}</h2>
            <ul className="list">
              {group.events.map((event) => (
                <li key={event.id} className="item">
                  <div className="grow">
                    <h3>{event.title}</h3>
                    <p className="event-time">
                      {timeText(event.startsAt)} – {timeText(event.endsAt)}
                      {event.location ? ` · ${event.location}` : ''}
                    </p>
                    {event.description && <p className="muted">{event.description}</p>}
                  </div>
                  <div className="actions">
                    <button
                      type="button"
                      className="secondary"
                      disabled={busy}
                      aria-label={`Edit ${event.title}`}
                      onClick={() => startEdit(event)}
                    >
                      Edit
                    </button>
                    <button
                      type="button"
                      className="danger"
                      disabled={busy}
                      aria-label={`Delete ${event.title}`}
                      onClick={() => removeEvent(event.id)}
                    >
                      Delete
                    </button>
                  </div>
                </li>
              ))}
            </ul>
          </div>
        ))}
    </section>
  );
}
