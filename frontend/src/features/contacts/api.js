import { http, withQuery } from '../../core/http.js';

export const contactsApi = {
  list: (q) => http.get(withQuery('/api/contacts', { q })),
  create: (contact) => http.post('/api/contacts', contact),
  update: (id, contact) => http.put(`/api/contacts/${id}`, contact),
  remove: (id) => http.delete(`/api/contacts/${id}`),
};
