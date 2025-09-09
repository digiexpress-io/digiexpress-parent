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
    userId: 'tester',
    email: 'super user @ super dot things',
    name: 'John Smith',
    //roles: ['super-role', 'role 2', 'admin', 'super-role2', 'role 3', 'admin-again'],
    permissions: [
      'WRENCH_VIEW', 'WRENCH_EDIT',
      'STENCIL_VIEW', 'STENCIL_EDIT',
      'FEEDBACK_VIEW', 'FEEBACK_EDIT',
      'DEPLOYMENT_VIEW', 'DEPLOYMENT_EDIT',
      'TASK_GROUP_VIEW', 'TASK_GROUP_EDIT',
      'RELEASE_VIEW', 'RELEASE_EDIT',
      'DIALOB_VIEW', 'DIALOB_EDIT',
      'DASHBOARD_VIEW',
      'TASK_ALL_VIEW', 'TASK_ALL_EDIT', 'TASK_ALL_DELETE',
      'TABLES_V2',
      'TASK_REOPEN',
      'NAV_TO_USER_PROFILE'
    ]
  }
})