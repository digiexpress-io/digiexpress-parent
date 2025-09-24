import { defineMock } from 'vite-plugin-mock-dev-server'


export default defineMock({
  url: '/wrench/api/tasks/v1/statistics/status',


  body: [
    {
      count: 3,
      status: 'NEW'
    }
  ]
}
)

