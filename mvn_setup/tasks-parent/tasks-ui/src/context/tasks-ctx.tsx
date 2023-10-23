import React from 'react';
import { TasksContextType, TasksMutator, TasksDispatch, TasksState } from './tasks-ctx-types';
import { TasksStateBuilder } from './tasks-ctx-impl';
import { Backend, Profile } from 'taskclient';
import { Palette } from 'taskdescriptor';

const TasksContext = React.createContext<TasksContextType>({} as TasksContextType);


const init: TasksState = new TasksStateBuilder({
  owners: [],
  roles: [],
  tasks: [],
  tasksByOwner: {},
  palette: {
    roles: {},
    owners: {},
    status: Palette.status,
    priority: Palette.priority
  },
  profile: { contentType: "OK", name: "", userId: "", today: new Date(), roles: [] }
});

const TasksProvider: React.FC<{ children: React.ReactNode, backend: Backend, profile: Profile }> = ({ children, backend, profile }) => {

  const [loading, setLoading] = React.useState<boolean>(true);
  const [state, setState] = React.useState<TasksState>(init.withProfile(profile));
  const setter: TasksDispatch = React.useCallback((mutator: TasksMutator) => setState(mutator), [setState]);

  const contextValue: TasksContextType = React.useMemo(() => {
    return {
      state, setState: setter, loading, palette: Palette, reload: async () => {
        backend.task.getActiveTasks().then(data => {
          setState(prev => prev.withTasks(data.records))
        });
      }
    };
  }, [state, setter, loading, setLoading]);

  React.useEffect(() => {
    if (!loading) {
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

