import { useState } from 'react';
import { describeError } from '../../core/http.js';
import { useResource } from '../../core/hooks.js';
import { Empty, ErrorBanner, Loading, PageHeader } from '../../core/ui.jsx';
import { bookmarksApi } from './api.js';

const EMPTY_FORM = { title: '', url: '', category: '' };

export default function BookmarksPage() {
  const [category, setCategory] = useState('');
  const [form, setForm] = useState(EMPTY_FORM);
  const [formError, setFormError] = useState('');
  const [busy, setBusy] = useState(false);
  const bookmarks = useResource(() => bookmarksApi.list(category), [category]);
  const categories = useResource(() => bookmarksApi.categories());

  async function run(action) {
    setBusy(true);
    setFormError('');
    try {
      await action();
      bookmarks.reload();
      categories.reload();
      return true;
    } catch (error) {
      setFormError(describeError(error));
      return false;
    } finally {
      setBusy(false);
    }
  }

  async function addBookmark(event) {
    event.preventDefault();
    const created = await run(() => bookmarksApi.create(form));
    if (created) setForm(EMPTY_FORM);
  }

  const knownCategories = categories.data ?? [];

  return (
    <section>
      <PageHeader title="Bookmarks" subtitle="Save links and group them by category." />

      <form className="panel inline" onSubmit={addBookmark}>
        <div className="field">
          <label htmlFor="bookmark-title">Title</label>
          <input
            id="bookmark-title"
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
            maxLength={200}
            required
          />
        </div>
        <div className="field wide">
          <label htmlFor="bookmark-url">URL</label>
          <input
            id="bookmark-url"
            type="url"
            placeholder="https://example.org"
            value={form.url}
            onChange={(e) => setForm({ ...form, url: e.target.value })}
            required
          />
        </div>
        <div className="field">
          <label htmlFor="bookmark-category">Category</label>
          <input
            id="bookmark-category"
            list="bookmark-categories"
            placeholder="General"
            value={form.category}
            onChange={(e) => setForm({ ...form, category: e.target.value })}
            maxLength={100}
          />
          <datalist id="bookmark-categories">
            {knownCategories.map((name) => (
              <option key={name} value={name} />
            ))}
          </datalist>
        </div>
        <button type="submit" disabled={busy}>
          Add bookmark
        </button>
      </form>
      {formError && (
        <div className="banner error" role="alert">
          {formError}
        </div>
      )}

      <div className="chips" role="group" aria-label="Filter by category">
        <button type="button" aria-pressed={category === ''} onClick={() => setCategory('')}>
          All
        </button>
        {knownCategories.map((name) => (
          <button key={name} type="button" aria-pressed={category === name} onClick={() => setCategory(name)}>
            {name}
          </button>
        ))}
      </div>

      <ErrorBanner error={bookmarks.error} />
      {bookmarks.loading && !bookmarks.data && <Loading />}
      {bookmarks.data && bookmarks.data.length === 0 && <Empty>No bookmarks here yet.</Empty>}
      {bookmarks.data && bookmarks.data.length > 0 && (
        <ul className="list">
          {bookmarks.data.map((bookmark) => (
            <li key={bookmark.id} className="item">
              <div className="grow">
                <h3>
                  <a href={bookmark.url} target="_blank" rel="noopener noreferrer">
                    {bookmark.title}
                  </a>
                </h3>
                <p className="muted">
                  {bookmark.category} · {bookmark.url}
                </p>
              </div>
              <div className="actions">
                <button
                  type="button"
                  className="danger"
                  disabled={busy}
                  aria-label={`Delete ${bookmark.title}`}
                  onClick={() => run(() => bookmarksApi.remove(bookmark.id))}
                >
                  Delete
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
