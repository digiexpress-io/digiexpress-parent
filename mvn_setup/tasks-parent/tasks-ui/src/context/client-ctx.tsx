import React from 'react';
import { Session, Actions } from './composer-ctx-types';
import TaskClient from 'taskclient';


export interface ComposerContextType {
  session: Session;
  actions: Actions;
}

export type ClientContextType = TaskClient.Backend;

export const ComposerContext = React.createContext<ComposerContextType>({
  session: {} as Session,
  actions: {} as Actions,
});
export const ClientContext = React.createContext<ClientContextType>({} as ClientContextType);

