import { useState } from 'react';
import { describeError } from '../../core/http.js';
import { useResource } from '../../core/hooks.js';
import { Empty, ErrorBanner, Loading, PageHeader } from '../../core/ui.jsx';
import { expensesApi } from './api.js';
import './expenses.css';

const pad = (value) => String(value).padStart(2, '0');

function todayIso() {
  const now = new Date();
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`;
}

const money = (value) => Number(value).toFixed(2);

export default function ExpensesPage() {
  const [month, setMonth] = useState(todayIso().slice(0, 7));
  const [form, setForm] = useState({ categoryId: '', description: '', amount: '', spentOn: todayIso() });
  const [formError, setFormError] = useState('');
  const [busy, setBusy] = useState(false);

  const categories = useResource(() => expensesApi.categories());
  const expenses = useResource(() => (month ? expensesApi.list(month) : Promise.resolve([])), [month]);
  const summary = useResource(() => (month ? expensesApi.summary(month) : Promise.resolve(null)), [month]);

  const categoryOptions = categories.data ?? [];
  const selectedCategory = form.categoryId || (categoryOptions[0] ? String(categoryOptions[0].id) : '');

  function reloadMonth() {
    expenses.reload();
    summary.reload();
  }

  async function run(action) {
    setBusy(true);
    setFormError('');
    try {
      await action();
      return true;
    } catch (error) {
      setFormError(describeError(error));
      return false;
    } finally {
      setBusy(false);
    }
  }

  async function addExpense(event) {
    event.preventDefault();
    const created = await run(() =>
      expensesApi.create({
        categoryId: Number(selectedCategory),
        description: form.description,
        amount: Number(form.amount),
        spentOn: form.spentOn,
      }),
    );
    if (!created) return;
    const addedMonth = form.spentOn.slice(0, 7);
    setForm({ ...form, description: '', amount: '' });
    if (addedMonth === month) reloadMonth();
    else setMonth(addedMonth); // jump to the month that now contains the new expense
  }

  async function removeExpense(id) {
    if (await run(() => expensesApi.remove(id))) reloadMonth();
  }

  return (
    <section>
      <PageHeader title="Expenses" subtitle="Log spending and see monthly totals per category." />

      <form className="panel inline" onSubmit={addExpense}>
        <div className="field wide">
          <label htmlFor="expense-description">Description</label>
          <input
            id="expense-description"
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
            maxLength={200}
            required
          />
        </div>
        <div className="field">
          <label htmlFor="expense-amount">Amount</label>
          <input
            id="expense-amount"
            type="number"
            min="0.01"
            step="0.01"
            value={form.amount}
            onChange={(e) => setForm({ ...form, amount: e.target.value })}
            required
          />
        </div>
        <div className="field">
          <label htmlFor="expense-category">Category</label>
          <select
            id="expense-category"
            value={selectedCategory}
            onChange={(e) => setForm({ ...form, categoryId: e.target.value })}
          >
            {categoryOptions.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </div>
        <div className="field">
          <label htmlFor="expense-date">Date</label>
          <input
            id="expense-date"
            type="date"
            value={form.spentOn}
            onChange={(e) => setForm({ ...form, spentOn: e.target.value })}
            required
          />
        </div>
        <button type="submit" disabled={busy || !selectedCategory}>
          Add expense
        </button>
      </form>
      {formError && (
        <div className="banner error" role="alert">
          {formError}
        </div>
      )}

      <div className="field" style={{ maxWidth: 220 }}>
        <label htmlFor="expense-month">Month</label>
        <input id="expense-month" type="month" value={month} onChange={(e) => setMonth(e.target.value)} />
      </div>

      <ErrorBanner error={summary.error || expenses.error || categories.error} />

      {summary.data && (
        <table className="summary-table" aria-label="Monthly summary" style={{ margin: '16px 0' }}>
          <caption>Totals for {summary.data.month}</caption>
          <thead>
            <tr>
              <th scope="col">Category</th>
              <th scope="col" className="amount">
                Total
              </th>
            </tr>
          </thead>
          <tbody>
            {summary.data.categories.length === 0 && (
              <tr>
                <td colSpan={2} className="muted">
                  Nothing spent this month.
                </td>
              </tr>
            )}
            {summary.data.categories.map((row) => (
              <tr key={row.category}>
                <td>{row.category}</td>
                <td className="amount">{money(row.total)}</td>
              </tr>
            ))}
          </tbody>
          <tfoot>
            <tr>
              <th scope="row">Grand total</th>
              <td className="amount">{money(summary.data.grandTotal)}</td>
            </tr>
          </tfoot>
        </table>
      )}

      {expenses.loading && !expenses.data && <Loading />}
      {expenses.data && expenses.data.length === 0 && <Empty>No expenses in this month.</Empty>}
      {expenses.data && expenses.data.length > 0 && (
        <ul className="list">
          {expenses.data.map((expense) => (
            <li key={expense.id} className="item">
              <div className="grow">
                <h3>{expense.description}</h3>
                <p className="muted">
                  {expense.categoryName} · {expense.spentOn}
                </p>
              </div>
              <div className="actions">
                <span className="expense-amount">{money(expense.amount)}</span>
                <button
                  type="button"
                  className="danger"
                  disabled={busy}
                  aria-label={`Delete ${expense.description}`}
                  onClick={() => removeExpense(expense.id)}
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
