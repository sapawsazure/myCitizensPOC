import { render, screen, within } from '@testing-library/react';
import { afterEach, expect, it, vi } from 'vitest';
import TimeTrackerPage from './TimeTrackerPage.jsx';

afterEach(() => vi.unstubAllGlobals());

const projects = [
  { id: 1, name: 'General' },
  { id: 2, name: 'Learning' },
];
const summary = {
  date: '2030-06-10',
  projects: [{ projectId: 2, project: 'Learning', seconds: 3725 }],
  totalSeconds: 3725,
};

function mockFetch(state) {
  vi.stubGlobal(
    'fetch',
    vi.fn((url) => {
      const payload = url.includes('/projects') ? projects : url.includes('/summary') ? summary : state;
      return Promise.resolve(
        new Response(JSON.stringify(payload), { status: 200, headers: { 'Content-Type': 'application/json' } }),
      );
    }),
  );
}

const stoppedEntry = {
  id: 7,
  projectId: 2,
  projectName: 'Learning',
  description: 'Read the Flyway docs',
  startedAt: '2030-06-10T08:00:00Z',
  endedAt: '2030-06-10T09:02:05Z',
  durationSeconds: 3725,
};

it('offers to start a timer when nothing is running and lists today', async () => {
  mockFetch({ date: '2030-06-10', running: null, today: [stoppedEntry] });
  render(<TimeTrackerPage />);

  expect(await screen.findByRole('button', { name: 'Start timer' })).toBeInTheDocument();
  expect(await screen.findByRole('option', { name: 'Learning' })).toBeInTheDocument();
  expect(await screen.findByText('Read the Flyway docs')).toBeInTheDocument();
  const totals = await screen.findByRole('table', { name: 'Time per project today' });
  expect(within(totals).getAllByText('1:02:05')).toHaveLength(2); // project row and total
  expect(screen.queryByRole('button', { name: 'Stop timer' })).not.toBeInTheDocument();
});

it('shows a live clock and a stop button while a timer runs', async () => {
  const startedAt = new Date(Date.now() - 65 * 1000).toISOString();
  mockFetch({
    date: '2030-06-10',
    running: { id: 8, projectId: 1, projectName: 'General', description: 'Pairing', startedAt, endedAt: null, durationSeconds: 65 },
    today: [],
  });
  render(<TimeTrackerPage />);

  expect(await screen.findByRole('button', { name: 'Stop timer' })).toBeInTheDocument();
  expect(screen.getByText(/Pairing/)).toBeInTheDocument();
  expect(screen.getByRole('timer')).toHaveTextContent(/^0:0[1-9]:\d{2}$/); // about 1 minute 5 seconds
  expect(screen.queryByRole('button', { name: 'Start timer' })).not.toBeInTheDocument();
});
