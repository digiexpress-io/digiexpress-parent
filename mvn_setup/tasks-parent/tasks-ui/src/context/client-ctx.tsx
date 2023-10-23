import React from 'react';
import TaskClient from 'taskclient';

export interface ComposerContextType {
  session: TaskClient.Session;
  actions: TaskClient.Actions;
}

export type ClientContextType = TaskClient.Client;

export const ComposerContext = React.createContext<ComposerContextType>({
  session: {} as TaskClient.Session,
  actions: {} as TaskClient.Actions,
});
export const ClientContext = React.createContext<ClientContextType>({} as ClientContextType);

