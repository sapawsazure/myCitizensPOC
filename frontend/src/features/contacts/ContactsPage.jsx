import { useState } from 'react';
import { describeError } from '../../core/http.js';
import { useDebounced, useResource } from '../../core/hooks.js';
import { Empty, ErrorBanner, Loading, PageHeader } from '../../core/ui.jsx';
import { contactsApi } from './api.js';

const EMPTY_FORM = { fullName: '', email: '', phone: '', company: '', notes: '' };

export default function ContactsPage() {
  const [search, setSearch] = useState('');
  const query = useDebounced(search);
  const [form, setForm] = useState(EMPTY_FORM);
  const [editingId, setEditingId] = useState(null);
  const [formError, setFormError] = useState('');
  const [busy, setBusy] = useState(false);
  const contacts = useResource(() => contactsApi.list(query), [query]);

  const setField = (field) => (event) => setForm({ ...form, [field]: event.target.value });

  async function run(action) {
    setBusy(true);
    setFormError('');
    try {
      await action();
      contacts.reload();
      return true;
    } catch (error) {
      setFormError(describeError(error));
      return false;
    } finally {
      setBusy(false);
    }
  }

  function startEdit(contact) {
    setEditingId(contact.id);
    setForm({
      fullName: contact.fullName,
      email: contact.email ?? '',
      phone: contact.phone ?? '',
      company: contact.company ?? '',
      notes: contact.notes ?? '',
    });
  }

  function reset() {
    setEditingId(null);
    setForm(EMPTY_FORM);
  }

  async function save(event) {
    event.preventDefault();
    const saved = await run(() =>
      editingId ? contactsApi.update(editingId, form) : contactsApi.create(form),
    );
    if (saved) reset();
  }

  return (
    <section>
      <PageHeader title="Contacts" subtitle="Keep an address book you can search." />

      <form className="panel inline" onSubmit={save}>
        <div className="field wide">
          <label htmlFor="contact-name">Full name</label>
          <input id="contact-name" value={form.fullName} onChange={setField('fullName')} maxLength={200} required />
        </div>
        <div className="field">
          <label htmlFor="contact-email">Email</label>
          <input id="contact-email" type="email" value={form.email} onChange={setField('email')} maxLength={200} />
        </div>
        <div className="field">
          <label htmlFor="contact-phone">Phone</label>
          <input id="contact-phone" value={form.phone} onChange={setField('phone')} maxLength={50} />
        </div>
        <div className="field">
          <label htmlFor="contact-company">Company</label>
          <input id="contact-company" value={form.company} onChange={setField('company')} maxLength={200} />
        </div>
        <div className="field wide">
          <label htmlFor="contact-notes">Notes</label>
          <input id="contact-notes" value={form.notes} onChange={setField('notes')} maxLength={5000} />
        </div>
        <div>
          <button type="submit" disabled={busy}>
            {editingId ? 'Save changes' : 'Add contact'}
          </button>{' '}
          {editingId && (
            <button type="button" className="secondary" onClick={reset}>
              Cancel
            </button>
          )}
        </div>
      </form>
      {formError && (
        <div className="banner error" role="alert">
          {formError}
        </div>
      )}

      <div className="field">
        <label htmlFor="contact-search">Search contacts</label>
        <input
          id="contact-search"
          type="search"
          value={search}
          placeholder="Search by name, email or company"
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      <ErrorBanner error={contacts.error} />
      {contacts.loading && !contacts.data && <Loading />}
      {contacts.data && contacts.data.length === 0 && (
        <Empty>{query ? 'No contacts match your search.' : 'No contacts yet.'}</Empty>
      )}
      {contacts.data && contacts.data.length > 0 && (
        <ul className="list" style={{ marginTop: 16 }}>
          {contacts.data.map((contact) => (
            <li key={contact.id} className="item">
              <div className="grow">
                <h3>{contact.fullName}</h3>
                <p className="muted">
                  {[contact.company, contact.phone].filter(Boolean).join(' · ')}
                  {contact.email && (
                    <>
                      {contact.company || contact.phone ? ' · ' : ''}
                      <a href={`mailto:${contact.email}`}>{contact.email}</a>
                    </>
                  )}
                </p>
                {contact.notes && <p>{contact.notes}</p>}
              </div>
              <div className="actions">
                <button
                  type="button"
                  className="secondary"
                  disabled={busy}
                  aria-label={`Edit ${contact.fullName}`}
                  onClick={() => startEdit(contact)}
                >
                  Edit
                </button>
                <button
                  type="button"
                  className="danger"
                  disabled={busy}
                  aria-label={`Delete ${contact.fullName}`}
                  onClick={() => run(() => contactsApi.remove(contact.id))}
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
