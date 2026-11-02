import { render, screen } from '@testing-library/react';
import { afterEach, expect, it, vi } from 'vitest';
import ContactsPage from './ContactsPage.jsx';

afterEach(() => vi.unstubAllGlobals());

function mockFetch(payload) {
  vi.stubGlobal(
    'fetch',
    vi.fn(() =>
      Promise.resolve(new Response(JSON.stringify(payload), { status: 200, headers: { 'Content-Type': 'application/json' } })),
    ),
  );
}

it('shows the contacts returned by the API', async () => {
  mockFetch([
    {
      id: 1,
      fullName: 'Asha Verma',
      email: 'asha@example.com',
      phone: '+91 98765 43210',
      company: 'Northwind',
      notes: 'Prefers email',
      createdAt: '2030-01-01T10:00:00Z',
    },
  ]);
  render(<ContactsPage />);

  expect(await screen.findByText('Asha Verma')).toBeInTheDocument();
  expect(screen.getByRole('link', { name: 'asha@example.com' })).toHaveAttribute('href', 'mailto:asha@example.com');
  expect(screen.getByText('Prefers email')).toBeInTheDocument();
  expect(screen.getByLabelText('Search contacts')).toBeInTheDocument();
  expect(screen.getByRole('button', { name: 'Edit Asha Verma' })).toBeInTheDocument();
});

it('shows an empty state', async () => {
  mockFetch([]);
  render(<ContactsPage />);
  expect(await screen.findByText('No contacts yet.')).toBeInTheDocument();
});
