import { Role } from 'descriptor-access-mgmt';



export const testRoles: Role[] = [
  {
    id: '7yh4dR', description: 'All office staff -- default user roles', name: 'Tasks user',
    permissions: [
      { id: '77aor77', name: 'frontoffice/tasks/edit', description: 'Update task data', status: 'IN_FORCE' },
      { id: 't47luau', name: 'frontoffice/tasks/crm', description: 'View crm data on assigned task', status: 'IN_FORCE' },
      { id: '5t3uhua', name: 'stencil/articles/edit', description: 'Edit existing articles in stencil', status: 'IN_FORCE' },
      { id: 'y71665d', name: 'wrench/flow/admin', description: 'Create and edit flows', status: 'IN_FORCE' }
    ],
    principals: [
      { id: 'user1', name: 'John Smith', email: 'john.smith@gmail.com', roles: [], status: 'IN_FORCE' },
      { id: 'user2', name: 'Amy Coolidge', email: 'amy.c.cool@gmail.com', roles: [], status: 'IN_FORCE' },
      { id: 'user3', name: 'Rodger Arrowhead', email: 'forestman@lumberjack.com', roles: [], status: 'IN_FORCE' }
    ], status: 'IN_FORCE'
  },

  {
    id: '6hs2uH', description: 'Task assignment and archiving, view all crm data', name: 'Tasks admin',
    permissions: [],
    principals: [],
    status: 'IN_FORCE'
  },

  {
    id: '652hhud', description: 'Developer access -- Wrench configuration', name: 'Developer-Wrench read/write',
    permissions: [
      { id: '662hhdd', name: 'frontoffice/wrench/view', description: 'View Wrench assets', status: 'IN_FORCE' },
      { id: '34dtara', name: 'frontoffice/wrench/update', description: 'Update Wrench assets', status: 'IN_FORCE' },
      { id: '12hhbdd', name: 'frontoffice/wrench/release', description: 'Create new Wrench releases', status: 'IN_FORCE' },
    ],
    principals: [
      { id: 'user1', name: 'John Smith', email: 'john.smith@gmail.com', roles: [], status: 'IN_FORCE' },
      { id: 'user2', name: 'Amy Coolidge', email: 'amy.c.cool@gmail.com', roles: [], status: 'IN_FORCE' },
      { id: 'user3', name: 'Rodger Arrowhead', email: 'forestman@lumberjack.com', roles: [], status: 'IN_FORCE' },
      { id: 'user4', name: 'Jerry Springer', email: 'jerry-springer@the-jerry-show.com', roles: [], status: 'IN_FORCE' },
      { id: 'user5', name: 'Terry Straights', email: 'terry.straights@gmail.com', roles: [], status: 'IN_FORCE' },


    ], status: 'IN_FORCE'
  },

  {
    id: '2hy2yK', description: 'Training purposes - view only Stencil', name: 'Content management training',
    permissions: [
      { id: '654uuu', name: 'frontoffice/stencil/view', description: 'View portal content in Stencil', status: 'IN_FORCE' },
    ],
    principals: [
      { id: 'user3', name: 'Rodger Arrowhead', email: 'forestman@lumberjack.com', roles: [], status: 'IN_FORCE' }
    ],
    status: 'IN_FORCE'
  }
];


