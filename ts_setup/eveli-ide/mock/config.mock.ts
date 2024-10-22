import { createSSEStream, defineMock } from 'vite-plugin-mock-dev-server'

export default defineMock({
  url: '/config',

  /*
  response: (req, res) => {
    const sse = createSSEStream(req, res)
    sse.write({ event: 'message', data: { message: 'hello world' } })
    sse.end()
  }*/

  body: {
    api: 'defineMock-api',
    tasksApiUrl: '/wrench/api/tasks/v1',
    dialobApiUrl: '',
    dialobComposerUrl: '',
    dialobSessionUrl: '',
    wrenchApiUrl: '',
    wrenchIdeUrl: '',
    feedbackKey: '',

    appVersion: '',
    contentRepositoryUrl: '',
    calendarUrl: '',

    taskDeleteGroups: [],
    taskAdminGroups: [],
  }
})