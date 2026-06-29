import { defineMock } from 'vite-plugin-mock-dev-server'


export default defineMock({
  url: '/userInfo',
  body: {
    authenticated: true,
    authorized: true,
    userId: 'John Smith',
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
      'USER_PROFILE_EDIT',
      'HEALTH_VIEW',
      'TAGOMI_EDIT',
      'CONTRACT_EDIT',
      'COCKPITS_EDIT',
      'IN_HOUSE'
    ]
  }
})