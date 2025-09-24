import { defineMock } from 'vite-plugin-mock-dev-server'

export default defineMock({
  url: '/config',

  body: {
    serviceUrl: '/',

    appVersion: '',
    feedbackKey: '',
    taskDeleteGroups: [],
    taskAdminGroups: [],

    modifiableAssets: true
  }
})