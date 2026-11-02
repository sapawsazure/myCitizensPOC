import { http, withQuery } from '../../core/http.js';

export const expensesApi = {
  list: (month) => http.get(withQuery('/api/expenses', { month })),
  summary: (month) => http.get(withQuery('/api/expenses/summary', { month })),
  categories: () => http.get('/api/expenses/categories'),
  create: (expense) => http.post('/api/expenses', expense),
  remove: (id) => http.delete(`/api/expenses/${id}`),
};
