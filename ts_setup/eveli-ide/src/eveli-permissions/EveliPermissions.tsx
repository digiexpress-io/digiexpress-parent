import React from 'react';


export type EveliPermissionType =
  'NAV_TO_WRENCH' |
  'NAV_TO_STENCIL' |
  'NAV_TO_TASKS' |
  'NAV_TO_DIALOB' |
  'NAV_TO_RELEASES' |
  'NAV_TO_DEPLOYMENTS' |
  'NAV_TO_TASK_GROUP' |

  'NAV_TO_TASKS_FEEDBACK' |
  'NAV_TO_TASKS_DASHBOARD' |
  'NAV_TO_TASKS_MONITORING' |
  'NAV_TO_TASKS_QUEUES' |

  'NAV_TO_STENCIL_ARTICLES' |
  'NAV_TO_STENCIL_SERVICES' |
  'NAV_TO_STENCIL_LINKS' |
  'NAV_TO_STENCIL_LOCALES' |
  'NAV_TO_STENCIL_TEMPLATES' |
  'NAV_TO_STENCIL_MIGRATIONS' |
  'NAV_TO_STENCIL_RELEASES' |

  'NAV_TO_WRENCH_FLOWS' |
  'NAV_TO_WRENCH_DECISIONS' |
  'NAV_TO_WRENCH_SERVICES' |
  'NAV_TO_WRENCH_DEBUG' |
  'NAV_TO_WRENCH_COMPARE' |
  'NAV_TO_WRENCH_RELEASES' |

  'CREATE_TASK' |
  'CREATE_STENCIL_ASSET' |
  'CREATE_WRENCH_ASSET' |

  'EDIT_WRENCH_ASSET' |
  'EDIT_STENCIL_ASSET' |

  'DELETE_TASK' |
  'DELETE_STENCIL_ASSET';


export const EveliPermissions: React.FC<{ children: React.ReactNode, id: EveliPermissionType | undefined }> = ({ children, id }) => {


  return (<div onClick={() => console.log('EveliPermissionType:', id)}>{children}</div>)
}