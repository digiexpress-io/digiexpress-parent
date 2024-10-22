import { defineMock } from 'vite-plugin-mock-dev-server'

export default defineMock({
  url: '/wrench/api/tasks/v1/statistics/task-overdue',


  body: [
    {
      count: 2,
      assignedId: "defineMock-assignedId"
    }
  ]
})


