import LOG_FACTORY, { LogLevel, LoggerFactory } from "logger"


declare global {
  interface Window {
    LOG_FACTORY:  LoggerFactory
  }
}

//const log = LoggerFactory.getLogger();
window.LOG_FACTORY = LOG_FACTORY;
window.LOGGER = {
  config: {
    format: 'STRING',
    level: 'ERROR',
    values: process.env.REACT_APP_LOCAL_DEV_MODE ? getLogProps() : {}
  }
}  
export function initLogging() {
}

function getLogProps(): Record<string, LogLevel> {
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
    'src/components-task'         : 'DEBUG',
    'src/components-tenant'       : 'ERROR',
    'src/components-user-profile' : 'ERROR',
  
    'src/descriptor-customer'     : 'DEBUG',
    'src/descriptor-organization' : 'ERROR',
    'src/descriptor-project'      : 'ERROR',
    'src/descriptor-task'         : 'DEBUG',
    'src/descriptor-avatar'       : 'ERROR',
    'src/descriptor-popper'       : 'ERROR',

    'src/descriptor-grouping'      : 'DEBUG',
    'src/descriptor-tabbing'       : 'DEBUG',
    'src/descriptor-prefs'         : 'DEBUG',
  
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