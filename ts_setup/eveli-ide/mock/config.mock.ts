import { defineMock } from 'vite-plugin-mock-dev-server'

export default defineMock({
  url: '/config',

  body: {
    api: '/api',
    tasksApiUrl: '/api/tasks/v1',


    appVersion: '',
    feedbackKey: '',
    taskDeleteGroups: [],
    taskAdminGroups: [],

    modifiableAssets: true
  }
})