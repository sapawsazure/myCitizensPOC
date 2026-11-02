import { useState } from 'react';
import { describeError } from '../../core/http.js';
import { useResource } from '../../core/hooks.js';
import { Empty, ErrorBanner, Loading, PageHeader } from '../../core/ui.jsx';
import { habitsApi } from './api.js';
import './habits.css';

function weekdayLetter(isoDate) {
  return new Date(`${isoDate}T12:00:00`).toLocaleDateString(undefined, { weekday: 'narrow' });
}

export default function HabitsPage() {
  const [name, setName] = useState('');
  const [formError, setFormError] = useState('');
  const [busy, setBusy] = useState(false);
  const habits = useResource(() => habitsApi.list());

  async function run(action) {
    setBusy(true);
    setFormError('');
    try {
      await action();
      habits.reload();
      return true;
    } catch (error) {
      setFormError(describeError(error));
      return false;
    } finally {
      setBusy(false);
    }
  }

  async function addHabit(event) {
    event.preventDefault();
    if (await run(() => habitsApi.create(name))) setName('');
  }

  return (
    <section>
      <PageHeader title="Habits" subtitle="Build streaks by checking in daily." />

      <form className="panel inline" onSubmit={addHabit}>
        <div className="field wide">
          <label htmlFor="habit-name">New habit</label>
          <input
            id="habit-name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            maxLength={100}
            placeholder="For example: Read 20 minutes"
            required
          />
        </div>
        <button type="submit" disabled={busy}>
          Add habit
        </button>
      </form>
      {formError && (
        <div className="banner error" role="alert">
          {formError}
        </div>
      )}

      <ErrorBanner error={habits.error} />
      {habits.loading && !habits.data && <Loading />}
      {habits.data && habits.data.length === 0 && <Empty>No habits yet.</Empty>}
      {habits.data && habits.data.length > 0 && (
        <ul className="list">
          {habits.data.map((habit) => {
            const today = habit.last7Days[habit.last7Days.length - 1].date;
            return (
              <li key={habit.id} className="item">
                <div className="grow">
                  <h3>{habit.name}</h3>
                  <p>
                    <span className={`streak${habit.currentStreak === 0 ? ' zero' : ''}`}>
                      Streak: {habit.currentStreak} {habit.currentStreak === 1 ? 'day' : 'days'}
                    </span>
                  </p>
                  <ol className="dots" aria-label={`Last 7 days for ${habit.name}`}>
                    {habit.last7Days.map((day) => (
                      <li
                        key={day.date}
                        className={`dot${day.checked ? ' on' : ''}`}
                        title={day.date}
                        aria-label={`${day.date}: ${day.checked ? 'checked' : 'not checked'}`}
                      >
                        {weekdayLetter(day.date)}
                      </li>
                    ))}
                  </ol>
                </div>
                <div className="actions">
                  {habit.checkedToday ? (
                    <button
                      type="button"
                      className="secondary"
                      disabled={busy}
                      onClick={() => run(() => habitsApi.removeCheckin(habit.id, today))}
                    >
                      Undo today&apos;s check-in
                    </button>
                  ) : (
                    <button type="button" disabled={busy} onClick={() => run(() => habitsApi.checkIn(habit.id))}>
                      Check in today
                    </button>
                  )}
                  <button
                    type="button"
                    className="danger"
                    disabled={busy}
                    aria-label={`Delete ${habit.name}`}
                    onClick={() => run(() => habitsApi.remove(habit.id))}
                  >
                    Delete
                  </button>
                </div>
              </li>
            );
          })}
        </ul>
      )}
    </section>
  );
}
