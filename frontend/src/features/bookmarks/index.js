import BookmarksPage from './BookmarksPage.jsx';

export default {
  key: 'bookmarks',
  name: 'Bookmarks',
  description: 'Save links and group them by category.',
  route: '/bookmarks',
  phase: 1,
  order: 30,
  component: BookmarksPage,
};
