import { ClientContextType, ComposerContextType } from './client-ctx';
import * as Hooks from './hooks';

import {
  Document
} from './composer-ctx-types';


export type { Document }
export { useComposer } from './hooks';

export {
  initSession, SessionData, ActionsImpl
} from './composer-ctx-impl';


declare namespace Context {
  export type {
    ClientContextType, ComposerContextType
  }
}



namespace Context {
  export const useBackend = Hooks.useBackend;
  export const useTenantConfig = Hooks.useTenantConfig;
  export const useTasks = Hooks.useTasks;
  export const useDialobTenant = Hooks.useDialobTenant;
  export const useProjects = Hooks.useProjects;
  export const useOrg = Hooks.useOrg;
  export const useAssignees = Hooks.useAssignees;
  export const useProjectUsers = Hooks.useProjectUsers;
  export const useRoles = Hooks.useRoles;
  export const useTaskEdit = Hooks.useTaskEdit;
  export const useSite = Hooks.useSite;
  export const useUnsaved = Hooks.useUnsaved;
  export const useComposer = Hooks.useComposer;
  export const useSession = Hooks.useSession;
  export const useNav = Hooks.useNav;
  export const useApp = Hooks.useApp;

}

export default Context;