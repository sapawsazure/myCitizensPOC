import { useCallback, useEffect, useState } from 'react';

/**
 * Loads data with `loader()` whenever `deps` change or `reload()` is called.
 * Keeps the previous data on screen while reloading to avoid flicker.
 */
export function useResource(loader, deps = []) {
  const [state, setState] = useState({ data: null, error: null, loading: true });
  const [version, setVersion] = useState(0);

  useEffect(() => {
    let cancelled = false;
    setState((previous) => ({ ...previous, loading: true, error: null }));
    loader()
      .then((data) => {
        if (!cancelled) setState({ data, error: null, loading: false });
      })
      .catch((error) => {
        if (!cancelled) setState((previous) => ({ ...previous, error, loading: false }));
      });
    return () => {
      cancelled = true;
    };
    // `loader` is intentionally not a dependency: callers list what should trigger a reload in `deps`.
  }, [...deps, version]);

  const reload = useCallback(() => setVersion((v) => v + 1), []);
  return { ...state, reload };
}

/** Returns `value` after it stopped changing for `delayMs` (used for search boxes). */
export function useDebounced(value, delayMs = 300) {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const timer = setTimeout(() => setDebounced(value), delayMs);
    return () => clearTimeout(timer);
  }, [value, delayMs]);
  return debounced;
}
