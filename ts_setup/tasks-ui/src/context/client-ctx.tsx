import React from 'react';
import { Session, Actions } from './composer-ctx-types';
import TaskClient from 'client';


export interface ComposerContextType {
  session: Session;
  actions: Actions;
}
export const ComposerContext = React.createContext<ComposerContextType>({
  session: {} as Session,
  actions: {} as Actions,
});

export type ClientContextType = TaskClient.Backend;
export const ClientContext = React.createContext<ClientContextType>({} as ClientContextType);
