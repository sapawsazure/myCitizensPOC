import { render, screen } from '@testing-library/react';
import { afterEach, expect, it, vi } from 'vitest';
import BookmarksPage from './BookmarksPage.jsx';

afterEach(() => vi.unstubAllGlobals());

function mockFetchByUrl(routes) {
  vi.stubGlobal(
    'fetch',
    vi.fn((url) => {
      const payload = url.includes('/categories') ? routes.categories : routes.list;
      return Promise.resolve(
        new Response(JSON.stringify(payload), { status: 200, headers: { 'Content-Type': 'application/json' } }),
      );
    }),
  );
}

it('shows bookmarks with safe external links and category chips', async () => {
  mockFetchByUrl({
    list: [{ id: 1, title: 'React docs', url: 'https://react.dev', category: 'Frontend', createdAt: '2030-01-01T10:00:00Z' }],
    categories: ['Frontend', 'Java'],
  });
  render(<BookmarksPage />);

  const link = await screen.findByRole('link', { name: 'React docs' });
  expect(link).toHaveAttribute('href', 'https://react.dev');
  expect(link).toHaveAttribute('target', '_blank');
  expect(link).toHaveAttribute('rel', 'noopener noreferrer');
  expect(await screen.findByRole('button', { name: 'Java' })).toBeInTheDocument();
});

it('shows an empty state', async () => {
  mockFetchByUrl({ list: [], categories: [] });
  render(<BookmarksPage />);
  expect(await screen.findByText('No bookmarks here yet.')).toBeInTheDocument();
});
