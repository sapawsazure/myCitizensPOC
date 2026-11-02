import { http, withQuery } from '../../core/http.js';

export const notesApi = {
  list: (q) => http.get(withQuery('/api/notes', { q })),
  create: (note) => http.post('/api/notes', note),
  update: (id, note) => http.put(`/api/notes/${id}`, note),
  remove: (id) => http.delete(`/api/notes/${id}`),
};
