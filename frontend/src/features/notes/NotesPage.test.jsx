import { render, screen } from '@testing-library/react';
import { afterEach, expect, it, vi } from 'vitest';
import NotesPage from './NotesPage.jsx';

afterEach(() => vi.unstubAllGlobals());

function mockFetch(payload) {
  vi.stubGlobal(
    'fetch',
    vi.fn(() =>
      Promise.resolve(new Response(JSON.stringify(payload), { status: 200, headers: { 'Content-Type': 'application/json' } })),
    ),
  );
}

it('shows the notes returned by the API', async () => {
  mockFetch([
    { id: 1, title: 'Meeting ideas', body: 'Agree on the release date', createdAt: '2030-01-01T10:00:00Z', updatedAt: '2030-01-02T10:00:00Z' },
  ]);
  render(<NotesPage />);

  expect(await screen.findByText('Meeting ideas')).toBeInTheDocument();
  expect(screen.getByText('Agree on the release date')).toBeInTheDocument();
  expect(screen.getByLabelText('Search notes')).toBeInTheDocument();
  expect(screen.getByRole('button', { name: 'Edit Meeting ideas' })).toBeInTheDocument();
});

it('shows an empty state', async () => {
  mockFetch([]);
  render(<NotesPage />);
  expect(await screen.findByText('No notes yet.')).toBeInTheDocument();
});
