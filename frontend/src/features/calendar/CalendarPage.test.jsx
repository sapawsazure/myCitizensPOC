import { fireEvent, render, screen } from '@testing-library/react';
import { afterEach, expect, it, vi } from 'vitest';
import CalendarPage from './CalendarPage.jsx';

afterEach(() => vi.unstubAllGlobals());

function mockFetch(payload) {
  const fetchMock = vi.fn(() =>
    Promise.resolve(new Response(JSON.stringify(payload), { status: 200, headers: { 'Content-Type': 'application/json' } })),
  );
  vi.stubGlobal('fetch', fetchMock);
  return fetchMock;
}

it('shows the events of the month grouped by day', async () => {
  mockFetch([
    {
      id: 1,
      title: 'Project review',
      description: 'Walk through Phase 3',
      location: 'Room 1',
      startsAt: '2030-06-10T09:00:00Z',
      endsAt: '2030-06-10T10:00:00Z',
    },
    { id: 2, title: 'Dinner', description: null, location: null, startsAt: '2030-06-12T17:00:00Z', endsAt: '2030-06-12T19:00:00Z' },
  ]);
  render(<CalendarPage />);

  expect(await screen.findByText('Project review')).toBeInTheDocument();
  expect(screen.getByText('Dinner')).toBeInTheDocument();
  expect(screen.getByText('Walk through Phase 3')).toBeInTheDocument();
  expect(screen.getAllByRole('heading', { level: 2 })).toHaveLength(2); // one heading per day
  expect(screen.getByRole('button', { name: 'Edit Dinner' })).toBeInTheDocument();
});

it('shows an empty state and loads another month when you navigate', async () => {
  const fetchMock = mockFetch([]);
  render(<CalendarPage />);

  expect(await screen.findByText('No events this month.')).toBeInTheDocument();
  const firstUrl = fetchMock.mock.calls[0][0];
  fireEvent.click(screen.getByRole('button', { name: 'Next month' }));
  await vi.waitFor(() => expect(fetchMock.mock.calls.length).toBeGreaterThan(1));
  expect(fetchMock.mock.calls[fetchMock.mock.calls.length - 1][0]).not.toBe(firstUrl);
});
