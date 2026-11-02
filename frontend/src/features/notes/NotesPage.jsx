import { useState } from 'react';
import { describeError } from '../../core/http.js';
import { useDebounced, useResource } from '../../core/hooks.js';
import { Empty, ErrorBanner, Loading, PageHeader } from '../../core/ui.jsx';
import { notesApi } from './api.js';

const EMPTY_FORM = { title: '', body: '' };

export default function NotesPage() {
  const [search, setSearch] = useState('');
  const query = useDebounced(search);
  const [form, setForm] = useState(EMPTY_FORM);
  const [editingId, setEditingId] = useState(null);
  const [formError, setFormError] = useState('');
  const [busy, setBusy] = useState(false);
  const notes = useResource(() => notesApi.list(query), [query]);

  async function run(action) {
    setBusy(true);
    setFormError('');
    try {
      await action();
      notes.reload();
      return true;
    } catch (error) {
      setFormError(describeError(error));
      return false;
    } finally {
      setBusy(false);
    }
  }

  function startEdit(note) {
    setEditingId(note.id);
    setForm({ title: note.title, body: note.body ?? '' });
  }

  function reset() {
    setEditingId(null);
    setForm(EMPTY_FORM);
  }

  async function save(event) {
    event.preventDefault();
    const saved = await run(() =>
      editingId ? notesApi.update(editingId, form) : notesApi.create(form),
    );
    if (saved) reset();
  }

  return (
    <section>
      <PageHeader title="Notes" subtitle="Write notes and search them instantly." />

      <form className="panel" onSubmit={save}>
        <div className="field">
          <label htmlFor="note-title">Title</label>
          <input
            id="note-title"
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
            maxLength={200}
            required
          />
        </div>
        <div className="field" style={{ marginTop: 12 }}>
          <label htmlFor="note-body">Note</label>
          <textarea id="note-body" value={form.body} onChange={(e) => setForm({ ...form, body: e.target.value })} />
        </div>
        <p>
          <button type="submit" disabled={busy}>
            {editingId ? 'Save changes' : 'Add note'}
          </button>{' '}
          {editingId && (
            <button type="button" className="secondary" onClick={reset}>
              Cancel
            </button>
          )}
        </p>
      </form>
      {formError && (
        <div className="banner error" role="alert">
          {formError}
        </div>
      )}

      <div className="field">
        <label htmlFor="note-search">Search notes</label>
        <input
          id="note-search"
          type="search"
          value={search}
          placeholder="Type to search titles and text"
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      <ErrorBanner error={notes.error} />
      {notes.loading && !notes.data && <Loading />}
      {notes.data && notes.data.length === 0 && <Empty>{query ? 'No notes match your search.' : 'No notes yet.'}</Empty>}
      {notes.data && notes.data.length > 0 && (
        <ul className="list" style={{ marginTop: 16 }}>
          {notes.data.map((note) => (
            <li key={note.id} className="item">
              <div className="grow">
                <h3>{note.title}</h3>
                {note.body && <p style={{ whiteSpace: 'pre-wrap' }}>{note.body}</p>}
                <p className="muted">Updated {new Date(note.updatedAt).toLocaleString()}</p>
              </div>
              <div className="actions">
                <button type="button" className="secondary" disabled={busy} onClick={() => startEdit(note)} aria-label={`Edit ${note.title}`}>
                  Edit
                </button>
                <button
                  type="button"
                  className="danger"
                  disabled={busy}
                  aria-label={`Delete ${note.title}`}
                  onClick={() => run(() => notesApi.remove(note.id))}
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
