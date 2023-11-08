import React from 'react';
import { TaskEditContextType, TaskEditMutator, TaskEditDispatch, TaskEditMutatorBuilder } from './context-task-edit-types';
import { TaskEditStateBuilder } from './context-task-edit-state';
import { TaskDescriptor } from './types';

const TaskEditContext = React.createContext<TaskEditContextType>({} as TaskEditContextType);


const TaskEditProvider: React.FC<{ children: React.ReactNode, task: TaskDescriptor }> = ({ children, task }) => {

  const [state, setState] = React.useState<TaskEditMutatorBuilder>(new TaskEditStateBuilder({ task, today: new Date(), events: [] }));
  const setter: TaskEditDispatch = React.useCallback((mutator: TaskEditMutator) => setState(mutator), [setState]);

  const contextValue: TaskEditContextType = React.useMemo(() => {
    return { state, setState: setter, loading: false };
  }, [state, setter]);

  React.useMemo(() => {
    setState(previous => previous.withTaskDescriptor(task));
  }, [task, setState]);


  return (<TaskEditContext.Provider value={contextValue}>{children}</TaskEditContext.Provider>);
};


export { TaskEditProvider, TaskEditContext };
