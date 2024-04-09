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
  export const useAm = Hooks.useAm;
  export const useBackend = Hooks.useBackend;
  export const useTasks = Hooks.useTasks;
  export const useDialobTenant = Hooks.useDialobTenant;
  export const useTaskEdit = Hooks.useTaskEdit;
  export const useUnsaved = Hooks.useUnsaved;
  export const useComposer = Hooks.useComposer;
  export const useSession = Hooks.useSession;
  export const useNav = Hooks.useNav;
  export const useApp = Hooks.useApp;

}

export default Context;