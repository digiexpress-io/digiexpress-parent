import { createSSEStream, defineMock } from 'vite-plugin-mock-dev-server'

export default defineMock({
  url: '/config',

  body: {
    api: '/api',
    tasksApiUrl: '/api/tasks/v1',
    wrenchApiUrl: '',
    wrenchIdeUrl: '',
    contentRepositoryUrl: '',


    appVersion: '',

    calendarUrl: '',

    dialobApiUrl: '',
    dialobComposerUrl: '',
    dialobSessionUrl: '',
    feedbackKey: '',
    taskDeleteGroups: [],
    taskAdminGroups: [],
  }
})