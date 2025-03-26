import { createSSEStream, defineMock } from 'vite-plugin-mock-dev-server'


export default defineMock({
  url: '/userInfo',

  /*
  response: (req, res) => {
    const sse = createSSEStream(req, res)
    sse.write({ event: 'message', data: { message: 'hello world' } })
    sse.end()
  }*/


  body: {
    authenticated: true,
    authorized: true,
    userId: 'vorst x viiner',
    email: 'super user @ super dot things',
    name: 'John Smith',
    permissions: [
      'WRENCH_VIEW', 'WRENCH_EDIT',
      'STENCIL_VIEW', 'STENCIL_EDIT',
      'TASK_ALL_VIEW', 'TASK_ALL_EDIT', 'TASK_ALL_DELETE',
      'TASK_GROUP_VIEW', 'TASK_GROUP_EDIT',
      'RELEASE_VIEW', 'RELEASE_EDIT',
      'DEPLOYMENT_VIEW', 'DEPLOYMENT_EDIT',
      'DASHBOARD_VIEW',
      'FEEDBACK_VIEW', 'FEEBACK_EDIT',
      'DIALOB_VIEW', 'DIALOB_EDIT']
  }
})