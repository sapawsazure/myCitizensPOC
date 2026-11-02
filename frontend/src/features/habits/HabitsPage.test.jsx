import { render, screen, within } from '@testing-library/react';
import { afterEach, expect, it, vi } from 'vitest';
import HabitsPage from './HabitsPage.jsx';

afterEach(() => vi.unstubAllGlobals());

function mockFetch(payload) {
  vi.stubGlobal(
    'fetch',
    vi.fn(() =>
      Promise.resolve(new Response(JSON.stringify(payload), { status: 200, headers: { 'Content-Type': 'application/json' } })),
    ),
  );
}

function week(checkedFlags) {
  return checkedFlags.map((checked, index) => ({ date: `2030-06-${String(9 + index).padStart(2, '0')}`, checked }));
}

it('shows streaks, the last 7 days and the right check-in button', async () => {
  mockFetch([
    {
      id: 1,
      name: 'Drink water',
      createdAt: '2030-06-01T08:00:00Z',
      currentStreak: 3,
      checkedToday: true,
      last7Days: week([false, false, false, false, true, true, true]),
    },
    {
      id: 2,
      name: 'Evening walk',
      createdAt: '2030-06-01T08:00:00Z',
      currentStreak: 0,
      checkedToday: false,
      last7Days: week([false, false, false, false, false, false, false]),
    },
  ]);
  render(<HabitsPage />);

  expect(await screen.findByText('Drink water')).toBeInTheDocument();
  expect(screen.getByText('Streak: 3 days')).toBeInTheDocument();
  expect(screen.getByText('Streak: 0 days')).toBeInTheDocument();
  expect(within(screen.getByRole('list', { name: 'Last 7 days for Drink water' })).getAllByRole('listitem')).toHaveLength(7);
  expect(screen.getByRole('button', { name: /Undo today.s check-in/ })).toBeInTheDocument();
  expect(screen.getByRole('button', { name: 'Check in today' })).toBeInTheDocument();
});

it('shows an empty state', async () => {
  mockFetch([]);
  render(<HabitsPage />);
  expect(await screen.findByText('No habits yet.')).toBeInTheDocument();
});
