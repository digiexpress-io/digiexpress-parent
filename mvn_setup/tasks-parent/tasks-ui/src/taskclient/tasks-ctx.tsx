import React from 'react';
import { TasksContextType, TasksMutator, TasksDispatch, TasksMutatorBuilder } from './tasks-ctx-types';
import { TasksStateBuilder, Pallette } from './tasks-ctx-impl';
import { Client } from './client-types';
import { Profile } from './profile-types';

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
  },
  profile: { contentType: "OK", name: "", userId: "", today: new Date(), roles: []}
});

const TasksProvider: React.FC<{ children: React.ReactNode, backend: Client, profile: Profile }> = ({ children, backend, profile }) => {
  
  const [loading, setLoading] = React.useState<boolean>(true);
  const [state, setState] = React.useState<TasksMutatorBuilder>(init.withProfile(profile));
  const setter: TasksDispatch = React.useCallback((mutator: TasksMutator) => setState(mutator), [setState]);

  const contextValue: TasksContextType = React.useMemo(() => {
    return { state, setState: setter, loading, pallette: Pallette };
  }, [state, setter, loading]);

  React.useEffect(() => {
    if(!loading) {
      return;
    }
    backend.task.getActiveTasks().then(data => {
      setLoading(false);
      setState(prev => prev.withProfile(profile).withTasks(data.records))
    });
    
  }, [loading, setLoading, backend, profile]);
  
  return (<TasksContext.Provider value={contextValue}>{children}</TasksContext.Provider>);
};


export { TasksProvider, TasksContext };

