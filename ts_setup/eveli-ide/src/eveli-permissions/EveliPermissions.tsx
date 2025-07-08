import { IamApi, useIam } from '@/api-iam';
import React from 'react';

function oneOf(type: IamApi.UserPermission[]): (input: IamApi.UserPermission) => boolean {
  return (input) => type.includes(input);
}

const EveliPermissionMapping = {
  'NAV_TO_WRENCH': oneOf(['WRENCH_VIEW', 'WRENCH_EDIT']),
  'NAV_TO_STENCIL': oneOf(['STENCIL_VIEW', 'STENCIL_EDIT']),
  'NAV_TO_TASKS': oneOf(['TASK_ALL_VIEW', 'TASK_ALL_EDIT']),
  'NAV_TO_DIALOB': oneOf(['DIALOB_VIEW', 'DIALOB_EDIT']),
  'NAV_TO_RELEASES': oneOf(['RELEASE_VIEW', 'RELEASE_EDIT']), //eveli publications
  'NAV_TO_TASK_GROUP': oneOf(['TASK_GROUP_VIEW', 'TASK_GROUP_EDIT']),

  'NAV_TO_TASKS_FEEDBACK': oneOf(['FEEDBACK_VIEW', 'FEEBACK_EDIT']),
  'NAV_TO_TASKS_DASHBOARD': oneOf(['TASK_ALL_VIEW', 'DASHBOARD_VIEW']),
  'NAV_TO_TASKS_MONITORING': oneOf(['TASK_ALL_VIEW', 'TASK_ALL_EDIT']),
  'NAV_TO_TASKS_QUEUES': oneOf(['TASK_ALL_VIEW', 'TASK_ALL_EDIT']),

  'NAV_TO_STENCIL_ARTICLES': oneOf(['STENCIL_VIEW', 'STENCIL_EDIT']),
  'NAV_TO_STENCIL_SERVICES': oneOf(['STENCIL_VIEW', 'STENCIL_EDIT']),
  'NAV_TO_STENCIL_LINKS': oneOf(['STENCIL_VIEW', 'STENCIL_EDIT']),
  'NAV_TO_STENCIL_LOCALES': oneOf(['STENCIL_VIEW', 'STENCIL_EDIT']),
  'NAV_TO_STENCIL_TEMPLATES': oneOf(['STENCIL_VIEW', 'STENCIL_EDIT']),
  'NAV_TO_STENCIL_MIGRATIONS': oneOf(['STENCIL_VIEW', 'STENCIL_EDIT']),
  'NAV_TO_STENCIL_ASSISTANCE': oneOf(['STENCIL_VIEW', 'STENCIL_EDIT']),
  'NAV_TO_STENCIL_RELEASES': oneOf(['STENCIL_VIEW', 'STENCIL_EDIT']),

  'NAV_TO_WRENCH_FLOWS': oneOf(['WRENCH_VIEW', 'WRENCH_EDIT']),
  'NAV_TO_WRENCH_DECISIONS': oneOf(['WRENCH_VIEW', 'WRENCH_EDIT']),
  'NAV_TO_WRENCH_SERVICES': oneOf(['WRENCH_VIEW', 'WRENCH_EDIT']),
  'NAV_TO_WRENCH_MIGRATIONS': oneOf(['WRENCH_VIEW', 'WRENCH_EDIT']),
  'NAV_TO_WRENCH_DEBUG': oneOf(['WRENCH_VIEW', 'WRENCH_EDIT']),
  'NAV_TO_WRENCH_COMPARE': oneOf(['WRENCH_VIEW', 'WRENCH_EDIT']),
  'NAV_TO_WRENCH_RELEASES': oneOf(['WRENCH_VIEW', 'WRENCH_EDIT']),

  'NAV_TO_BATCHES': oneOf(['BATCH_VIEW', 'BATCH_EDIT']),

  'CREATE_TASK': oneOf(['TASK_ALL_VIEW', 'TASK_ALL_EDIT']),
  'CREATE_STENCIL_ASSET': oneOf(['STENCIL_VIEW', 'STENCIL_EDIT']),
  'CREATE_WRENCH_ASSET': oneOf(['WRENCH_VIEW', 'WRENCH_EDIT']),
  'CREATE_EVELI_PUBLICATION': oneOf([
    'STENCIL_VIEW', 'STENCIL_EDIT', 'WRENCH_VIEW', 'WRENCH_EDIT',
    'TASK_ALL_VIEW', 'TASK_ALL_EDIT', 'RELEASE_VIEW', 'RELEASE_EDIT'
  ]),


  'EXPORT_EVELI_PUBLICATION': oneOf([
    'STENCIL_VIEW', 'STENCIL_EDIT', 'WRENCH_VIEW', 'WRENCH_EDIT',
    'TASK_ALL_VIEW', 'TASK_ALL_EDIT', 'RELEASE_VIEW', 'RELEASE_EDIT'
  ]),

  'EDIT_WRENCH_ASSET': oneOf(['WRENCH_VIEW', 'WRENCH_EDIT']),
  'EDIT_STENCIL_ASSET': oneOf(['STENCIL_VIEW', 'STENCIL_EDIT']),

  'DELETE_TASK': oneOf(['TASK_ALL_VIEW', 'TASK_ALL_EDIT', 'TASK_ALL_DELETE']),
  'DELETE_STENCIL_ASSET': oneOf(['STENCIL_VIEW', 'STENCIL_EDIT']),

  'TASK_REOPEN': oneOf(['TASK_REOPEN']),

  'NAV_TO_TABLES_V2': oneOf(['TABLES_V2'])
}

export type EveliPermissionType = keyof typeof EveliPermissionMapping;


export const EveliPermissions: React.FC<{ children: React.ReactNode, id: EveliPermissionType }> = ({ children, id }) => {
  const { user } = useIam();
  const required = EveliPermissionMapping[id];
  const isAccessGranted = user.permissions.find((permission) => required(permission));
  if (isAccessGranted) {
    return <>{children}</>
  }

  return (<></>)
}