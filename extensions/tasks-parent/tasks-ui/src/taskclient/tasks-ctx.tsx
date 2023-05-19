import React from 'react';
import { TasksContextType, TasksMutator, TasksDispatch, TasksMutatorBuilder } from './tasks-ctx-types';
import { TasksStateBuilder, Pallette } from './tasks-ctx-impl';
import { Client } from './client-types';

const TasksContext = React.createContext<TasksContextType>({} as TasksContextType);


const init: TasksMutatorBuilder = new TasksStateBuilder({
  filterBy: [],
  groupBy: 'status',
  owners: [],
  roles: [],
  filtered: [],
  groups: [],
  tasks: [],
  tasksByOwner: {},
  searchString: undefined,
  pallette: {
    roles: {},
    owners: {},
    status: Pallette.status,
    priority: Pallette.priority
  }
});

const TasksProvider: React.FC<{ children: React.ReactNode, backend: Client }> = ({ children, backend }) => {
  
  const [loading, setLoading] = React.useState<boolean>(true);
  const [state, setState] = React.useState<TasksMutatorBuilder>(init);
  const setter: TasksDispatch = React.useCallback((mutator: TasksMutator) => setState(mutator), [setState]);

  const contextValue: TasksContextType = React.useMemo(() => {
    return { state, setState: setter, loading, pallette: Pallette };
  }, [state, setter, loading]);

  React.useEffect(() => {
    if(!loading) {
      return;
    }
    backend.active().then(data => {
      setLoading(false);
      setState(prev => prev.withTasks(data.records))
    });
    
  }, [loading, setLoading]);
  

  return (<TasksContext.Provider value={contextValue}>{children}</TasksContext.Provider>);
};


export { TasksProvider, TasksContext };

