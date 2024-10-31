import { defineMock } from 'vite-plugin-mock-dev-server'

export default defineMock({
  url: '/config',

  body: {
    api: '/api',
    tasksApiUrl: '/api/tasks/v1',
    dialobComposerUrl: '/dialob/composer',

    wrenchApiUrl: '',
    wrenchIdeUrl: '',
    contentRepositoryUrl: '',

    appVersion: '',

    calendarUrl: '',

    dialobApiUrl: '',

    dialobSessionUrl: '',
    feedbackKey: '',
    taskDeleteGroups: [],
    taskAdminGroups: [],

    modifiableAssets: true
  }
})