import { Link } from 'react-router-dom';
import { features } from './registry.js';
import { ErrorBanner } from './ui.jsx';

function countLabel(live, loading) {
  if (live) return `${live.itemCount} ${live.itemCount === 1 ? 'item' : 'items'} stored`;
  return loading ? 'Loading…' : 'Backend module missing';
}

/** Landing page: one card per feature module found in this build. */
export default function Home({ backend }) {
  const { data, error, loading } = backend;
  const backendFeatures = data?.features ?? [];
  const backendByKey = new Map(backendFeatures.map((feature) => [feature.key, feature]));
  const frontendKeys = new Set(features.map((feature) => feature.key));

  const missingInBackend = features.filter((f) => !backendByKey.has(f.key)).map((f) => f.name);
  const missingInFrontend = backendFeatures.filter((f) => !frontendKeys.has(f.key)).map((f) => f.name);
  const outOfSync = data && (data.count !== features.length || missingInBackend.length > 0 || missingInFrontend.length > 0);

  return (
    <section>
      <header className="hero">
        <h1>Productivity Hub</h1>
        <p className="lead">
          <strong>{features.length}</strong> {features.length === 1 ? 'feature' : 'features'}, each backed by its own
          PostgreSQL tables.
        </p>
      </header>

      <ErrorBanner error={error} />
      {outOfSync && (
        <div className="banner warn" role="alert">
          Frontend and backend are out of sync
          {missingInBackend.length > 0 && <> — missing in backend: {missingInBackend.join(', ')}</>}
          {missingInFrontend.length > 0 && <> — missing in frontend: {missingInFrontend.join(', ')}</>}. Was a phase
          only partly merged?
        </div>
      )}

      <div className="cards">
        {features.map((feature) => (
          <article key={feature.key} className="card">
            <span className="badge">Added in Phase {feature.phase}</span>
            <h2>
              <Link to={feature.route}>{feature.name}</Link>
            </h2>
            <p>{feature.description}</p>
            <p className="muted">{countLabel(backendByKey.get(feature.key), loading)}</p>
          </article>
        ))}
      </div>

      <p className="meta">
        Frontend modules: {features.length} · Backend modules: {data ? data.count : '–'}
      </p>
    </section>
  );
}
