import { http, withQuery } from '../../core/http.js';

export const tasksApi = {
  list: (status) => http.get(withQuery('/api/tasks', { status })),
  create: (task) => http.post('/api/tasks', task),
  setStatus: (id, status) => http.patch(`/api/tasks/${id}/status`, { status }),
  remove: (id) => http.delete(`/api/tasks/${id}`),
};
