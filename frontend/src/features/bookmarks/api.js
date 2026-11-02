import { http, withQuery } from '../../core/http.js';

export const bookmarksApi = {
  list: (category) => http.get(withQuery('/api/bookmarks', { category })),
  categories: () => http.get('/api/bookmarks/categories'),
  create: (bookmark) => http.post('/api/bookmarks', bookmark),
  remove: (id) => http.delete(`/api/bookmarks/${id}`),
};
