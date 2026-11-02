import { http } from '../../core/http.js';

export const habitsApi = {
  list: () => http.get('/api/habits'),
  create: (name) => http.post('/api/habits', { name }),
  remove: (id) => http.delete(`/api/habits/${id}`),
  checkIn: (id) => http.post(`/api/habits/${id}/checkins`),
  removeCheckin: (id, date) => http.delete(`/api/habits/${id}/checkins/${date}`),
};
