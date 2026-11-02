import { describeError } from './http.js';

export function PageHeader({ title, subtitle }) {
  return (
    <header className="page-header">
      <h1>{title}</h1>
      {subtitle && <p className="muted">{subtitle}</p>}
    </header>
  );
}

export function Loading({ label = 'Loading…' }) {
  return (
    <p className="muted" role="status">
      {label}
    </p>
  );
}

export function ErrorBanner({ error }) {
  if (!error) return null;
  return (
    <div className="banner error" role="alert">
      {describeError(error)}
    </div>
  );
}

export function Empty({ children }) {
  return <p className="empty">{children}</p>;
}
