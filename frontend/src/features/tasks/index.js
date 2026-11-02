import TasksPage from './TasksPage.jsx';

export default {
  key: 'tasks',
  name: 'Tasks',
  description: 'Track to-dos with status, priority and due date.',
  route: '/tasks',
  phase: 1,
  order: 10,
  component: TasksPage,
};
