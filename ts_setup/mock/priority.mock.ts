import { defineMock } from 'vite-plugin-mock-dev-server'


export default defineMock({
  url: '/wrench/api/tasks/v1/statistics/priority',


  body: [
    {
      count: 2,
      priority: 'NORMAL'
    }
  ]
}
)

