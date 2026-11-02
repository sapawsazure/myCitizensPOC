import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { expect, it } from 'vitest';
import Home from './Home.jsx';
import { features } from './registry.js';

function renderHome(backend) {
  return render(
    <MemoryRouter>
      <Home backend={backend} />
    </MemoryRouter>,
  );
}

const liveBackend = {
  data: {
    appVersion: '1.0.0',
    count: features.length,
    features: features.map((feature) => ({ key: feature.key, itemCount: 4 })),
  },
  error: null,
  loading: false,
};

it('renders one card per discovered feature module', () => {
  renderHome(liveBackend);
  expect(features.length).toBeGreaterThan(0);
  expect(screen.getAllByRole('article')).toHaveLength(features.length);
  features.forEach((feature) => {
    expect(screen.getByRole('link', { name: feature.name })).toHaveAttribute('href', feature.route);
  });
  expect(screen.queryByRole('alert')).not.toBeInTheDocument();
});

it('warns when the backend and frontend modules differ', () => {
  renderHome({ ...liveBackend, data: { ...liveBackend.data, count: features.length - 1, features: [] } });
  expect(screen.getByRole('alert')).toHaveTextContent('out of sync');
});
