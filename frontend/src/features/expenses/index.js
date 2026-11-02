import ExpensesPage from './ExpensesPage.jsx';

export default {
  key: 'expenses',
  name: 'Expenses',
  description: 'Log spending and see monthly totals per category.',
  route: '/expenses',
  phase: 2,
  order: 40,
  component: ExpensesPage,
};
