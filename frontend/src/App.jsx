import { Link, NavLink, Route, Routes } from 'react-router-dom';
import { features } from './core/registry.js';
import { http } from './core/http.js';
import { useResource } from './core/hooks.js';
import Home from './core/Home.jsx';

function NotFound() {
  return (
    <section>
      <h1>Page not found</h1>
      <p>
        <Link to="/">Back to the home page</Link>
      </p>
    </section>
  );
}

export default function App() {
  // One request for the whole shell: live item counts, backend module list and app version.
  const backend = useResource(() => http.get('/api/features'));

  return (
    <div className="app">
      <header className="topbar">
        <Link to="/" className="brand">
          Productivity Hub
        </Link>
        <nav aria-label="Features">
          <NavLink to="/" end>
            Home
          </NavLink>
          {features.map((feature) => (
            <NavLink key={feature.key} to={feature.route}>
              {feature.name}
            </NavLink>
          ))}
        </nav>
      </header>

      <main>
        <Routes>
          <Route path="/" element={<Home backend={backend} />} />
          {features.map((feature) => (
            <Route key={feature.key} path={feature.route} element={<feature.component />} />
          ))}
          <Route path="*" element={<NotFound />} />
        </Routes>
      </main>

      <footer className="footer">
        Productivity Hub{backend.data ? ` v${backend.data.appVersion}` : ''} · {features.length} features
      </footer>
    </div>
  );
}
