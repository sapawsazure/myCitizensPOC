import { http, withQuery } from '../../core/http.js';

export const timeTrackerApi = {
  state: () => http.get('/api/timetracker'),
  start: (projectId, description) => http.post('/api/timetracker/start', { projectId, description }),
  stop: () => http.post('/api/timetracker/stop'),
  summary: (date) => http.get(withQuery('/api/timetracker/summary', { date })),
  projects: () => http.get('/api/timetracker/projects'),
  addProject: (name) => http.post('/api/timetracker/projects', { name }),
};
