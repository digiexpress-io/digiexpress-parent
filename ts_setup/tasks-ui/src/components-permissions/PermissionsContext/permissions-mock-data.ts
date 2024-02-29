import { Role } from 'descriptor-permissions';



export const testRoles: Role[] = [
  {
    id: '7yh4dR', description: 'All office staff -- default user roles', name: 'Tasks user',
    permissions: [
      { id: '77aor77', name: 'frontoffice/tasks/edit', description: 'Update task data', status: 'ENABLED' },
      { id: 't47luau', name: 'frontoffice/tasks/crm', description: 'View crm data on assigned task', status: 'ENABLED' }
    ],
    principals: [
      { id: 'user1', name: 'John Smith', email: 'john.smith@gmail.com', roles: [], status: 'ENABLED' },
      { id: 'user2', name: 'Amy Coolidge', email: 'amy.c.cool@gmail.com', roles: [], status: 'ENABLED' },
      { id: 'user3', name: 'Rodger Arrowhead', email: 'forestman@lumberjack.com', roles: [], status: 'ENABLED' }
    ], status: 'ENABLED'
  },

  {
    id: '6hs2uH', description: 'Task assignment and archiving, view all crm data', name: 'Tasks admin',
    permissions: [],
    principals: [],
    status: 'ENABLED'
  },

  {
    id: '2hy2yK', description: 'Training purposes - view only Stencil', name: 'Training - content management',
    permissions: [
      { id: '77aor77', name: 'frontoffice/stencil/view', description: 'View portal content in Stencil', status: 'ENABLED' },
    ],
    principals: [
      { id: 'user3', name: 'Rodger Arrowhead', email: 'forestman@lumberjack.com', roles: [], status: 'ENABLED' }
    ],
    status: 'ENABLED'
  }
];


