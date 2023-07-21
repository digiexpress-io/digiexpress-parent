import React from 'react';


import { Client } from './client-types';
import { Session, Actions } from './composer-types';

export interface ComposerContextType {
  session: Session;
  actions: Actions;
}

export type ClientContextType = Client;

export const ComposerContext = React.createContext<ComposerContextType>({
  session: {} as Session,
  actions: {} as Actions,
});
export const ClientContext = React.createContext<ClientContextType>({} as ClientContextType);

