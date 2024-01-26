import { LogLevel } from "logger"

export function getLogProps(): Record<string, LogLevel> {
  return {
    'src/components-burger'       : 'ERROR',
    'src/components-colors'       : 'ERROR',
    'src/components-customer'     : 'ERROR',
    'src/components-dialob'       : 'ERROR',
    'src/components-generic'      : 'ERROR',
    'src/components-hdes'         : 'ERROR',
    'src/components-project'      : 'ERROR',
    'src/components-stencil'      : 'ERROR',
    'src/components-sys-config'   : 'ERROR',
    'src/components-task'         : 'ERROR',
    'src/components-tenant'       : 'ERROR',
    'src/components-user-profile' : 'ERROR',
  
    'src/descriptor-customer'     : 'DEBUG',
    'src/descriptor-organization' : 'ERROR',
    'src/descriptor-project'      : 'ERROR',
    'src/descriptor-task'         : 'DEBUG',
    'src/descriptor-tenant'       : 'DEBUG',
    'src/descriptor-tenant-config' : 'ERROR',
    'src/descriptor-user-profile' : 'ERROR',
    'src/descriptor-avatar'       : 'ERROR',
    'src/descriptor-popper'       : 'ERROR',
  
    'src/client'    : 'ERROR',
    'src/context'   : 'ERROR',
    'src/table'     : 'DEBUG',
    'src/logger'    : 'ERROR',
  
    'src/app-frontoffice' : 'DEBUG',
    'src/app-hdes'        : 'ERROR',
    'src/application'     : 'ERROR',
    'src/app-projects'    : 'ERROR',
    'src/app-stencil'     : 'ERROR',
    'src/app-tasks'       : 'ERROR',
    'src/app-tenant'      : 'ERROR',
  }
}