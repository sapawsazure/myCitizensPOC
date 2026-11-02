import { render, screen } from '@testing-library/react';
import { afterEach, expect, it, vi } from 'vitest';
import TasksPage from './TasksPage.jsx';

afterEach(() => vi.unstubAllGlobals());

function mockFetch(payload) {
  vi.stubGlobal(
    'fetch',
    vi.fn(() =>
      Promise.resolve(new Response(JSON.stringify(payload), { status: 200, headers: { 'Content-Type': 'application/json' } })),
    ),
  );
}

it('shows the tasks returned by the API', async () => {
  mockFetch([
    { id: 1, title: 'Write the docs', status: 'TODO', priority: 1, dueDate: '2030-01-15', description: null },
    { id: 2, title: 'Ship the release', status: 'DONE', priority: 2, dueDate: null, description: null },
  ]);
  render(<TasksPage />);

  expect(await screen.findByText('Write the docs')).toBeInTheDocument();
  expect(screen.getByText('Ship the release')).toBeInTheDocument();
  expect(screen.getByRole('button', { name: 'Delete Write the docs' })).toBeInTheDocument();
  expect(screen.getByRole('button', { name: 'Add task' })).toBeInTheDocument();
});

it('shows an empty state', async () => {
  mockFetch([]);
  render(<TasksPage />);
  expect(await screen.findByText('No tasks here yet.')).toBeInTheDocument();
});
