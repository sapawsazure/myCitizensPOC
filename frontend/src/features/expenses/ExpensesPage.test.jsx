import { render, screen, within } from '@testing-library/react';
import { afterEach, expect, it, vi } from 'vitest';
import ExpensesPage from './ExpensesPage.jsx';

afterEach(() => vi.unstubAllGlobals());

function mockFetch({ list, summary }) {
  vi.stubGlobal(
    'fetch',
    vi.fn((url) => {
      const payload = url.includes('/summary')
        ? summary
        : url.includes('/categories')
          ? [{ id: 1, name: 'Food' }, { id: 2, name: 'Transport' }]
          : list;
      return Promise.resolve(
        new Response(JSON.stringify(payload), { status: 200, headers: { 'Content-Type': 'application/json' } }),
      );
    }),
  );
}

it('shows expenses and the monthly summary with totals', async () => {
  mockFetch({
    list: [{ id: 1, categoryId: 1, categoryName: 'Food', description: 'Team lunch', amount: 12.5, spentOn: '2030-01-15' }],
    summary: { month: '2030-01', categories: [{ category: 'Food', total: 12.5 }], grandTotal: 12.5 },
  });
  render(<ExpensesPage />);

  expect(await screen.findByText('Team lunch')).toBeInTheDocument();
  const table = await screen.findByRole('table', { name: 'Monthly summary' });
  expect(within(table).getByText('Food')).toBeInTheDocument();
  expect(within(table).getAllByText('12.50')).toHaveLength(2); // category total and grand total
  expect(screen.getByRole('button', { name: 'Delete Team lunch' })).toBeInTheDocument();
  expect(await screen.findByRole('option', { name: 'Transport' })).toBeInTheDocument();
});

it('shows an empty state', async () => {
  mockFetch({ list: [], summary: { month: '2030-01', categories: [], grandTotal: 0 } });
  render(<ExpensesPage />);
  expect(await screen.findByText('No expenses in this month.')).toBeInTheDocument();
  expect(await screen.findByText('Nothing spent this month.')).toBeInTheDocument();
});
