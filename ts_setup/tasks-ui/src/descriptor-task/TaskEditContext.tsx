import React from 'react';

import Context from 'context';
import { Task } from './backend-types';
import { TaskEditContextType } from './descriptor-types';
import { ImmutableTaskDescriptor } from './ImmutableTaskDescriptor';
import { ImmutableTaskEditEvents } from './ImmutableTaskEditEvents';


export const TaskEditContext = React.createContext<TaskEditContextType>({} as TaskEditContextType);

function today() {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return today;
}

type WithTask = (task: Task) => void;

export const TaskEditProvider: React.FC<{ children: React.ReactNode,  task: Task }> = ({ children, task }) => {
  const { profile } = Context.useOrg();

  const [loading, setLoading] = React.useState<boolean>(true);
  const [desc, setDesc] = React.useState<ImmutableTaskDescriptor>(new ImmutableTaskDescriptor(task, profile, today()));
  const [events, setEvents] = React.useState(new ImmutableTaskEditEvents(desc).build());

  const withTask: WithTask = React.useCallback((task: Task) => {
    const next = new ImmutableTaskDescriptor(task, profile, today());
    const nextEvents = new ImmutableTaskEditEvents(next).build();

    setDesc(next);
    setEvents(nextEvents);
  }, [setDesc, profile]);

  const contextValue: TaskEditContextType = React.useMemo(() => {
    return { loading, task: desc, events, withTask };
  }, [loading, desc, events, withTask]);

  return (<TaskEditContext.Provider value={contextValue}>{children}</TaskEditContext.Provider>);
}

