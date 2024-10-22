import { defineMock } from 'vite-plugin-mock-dev-server'


export default defineMock({
  url: '/wrench/api/tasks/v1/statistics/status-timeline',


  body: [
    {
      statusDate: "2024-11-11", //Date
      new: 3,
      open: 1,
      completed: 8,
      rejected: 3
    }
  ]
}
)

