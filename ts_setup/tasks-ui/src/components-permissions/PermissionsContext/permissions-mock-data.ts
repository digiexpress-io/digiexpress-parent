import { Role } from 'descriptor-permissions';



export const testRoles: Role[] = [
  {
    id: '7yh4dR', description: 'All office staff -- default user roles', name: 'Tasks user',
    permissions: [
      { id: '77aor77', name: 'frontoffice/tasks/edit', description: 'Update task data', status: 'ENABLED' },
      { id: 't47luau', name: 'frontoffice/tasks/crm', description: 'View crm data on assigned task', status: 'ENABLED' },
      { id: '5t3uhua', name: 'stencil/articles/edit', description: 'Edit existing articles in stencil', status: 'ENABLED' },
      { id: 'y71665d', name: 'wrench/flow/admin', description: 'Create and edit flows', status: 'ENABLED' }
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
    id: '652hhud', description: 'Developer access -- Wrench configuration', name: 'Developer-Wrench read/write',
    permissions: [
      { id: '662hhdd', name: 'frontoffice/wrench/view', description: 'View Wrench assets', status: 'ENABLED' },
      { id: '34dtara', name: 'frontoffice/wrench/update', description: 'Update Wrench assets', status: 'ENABLED' },
      { id: '12hhbdd', name: 'frontoffice/wrench/release', description: 'Create new Wrench releases', status: 'ENABLED' },
    ],
    principals: [
      { id: 'user1', name: 'John Smith', email: 'john.smith@gmail.com', roles: [], status: 'ENABLED' },
      { id: 'user2', name: 'Amy Coolidge', email: 'amy.c.cool@gmail.com', roles: [], status: 'ENABLED' },
      { id: 'user3', name: 'Rodger Arrowhead', email: 'forestman@lumberjack.com', roles: [], status: 'ENABLED' }
    ], status: 'ENABLED'
  },

  {
    id: '2hy2yK', description: 'Training purposes - view only Stencil', name: 'Content management training',
    permissions: [
      { id: '654uuu', name: 'frontoffice/stencil/view', description: 'View portal content in Stencil', status: 'ENABLED' },
    ],
    principals: [
      { id: 'user3', name: 'Rodger Arrowhead', email: 'forestman@lumberjack.com', roles: [], status: 'ENABLED' }
    ],
    status: 'ENABLED'
  }
];


