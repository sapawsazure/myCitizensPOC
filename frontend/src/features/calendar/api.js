import { http, withQuery } from '../../core/http.js';

export const calendarApi = {
  list: (month) => http.get(withQuery('/api/calendar', { month })),
  create: (event) => http.post('/api/calendar', event),
  update: (id, event) => http.put(`/api/calendar/${id}`, event),
  remove: (id) => http.delete(`/api/calendar/${id}`),
};
