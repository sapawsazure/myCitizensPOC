import TimeTrackerPage from './TimeTrackerPage.jsx';

export default {
  key: 'timetracker',
  name: 'Time Tracker',
  description: 'Time your work with a start/stop timer per project.',
  route: '/timetracker',
  phase: 3,
  order: 80,
  component: TimeTrackerPage,
};
