import React from 'react';
import { TaskEditContextType, TaskEditMutator, TaskEditDispatch, TaskEditMutatorBuilder } from './task-edit-ctx-types';
import { TaskEditStateBuilder } from './task-edit-ctx-impl';
import { TaskDescriptor } from 'taskdescriptor';

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

