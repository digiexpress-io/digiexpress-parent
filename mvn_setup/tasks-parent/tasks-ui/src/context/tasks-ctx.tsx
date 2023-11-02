import React from 'react';
import { TasksContextType, TasksMutator, TasksDispatch, TasksState } from './tasks-ctx-types';
import { TasksStateBuilder } from './tasks-ctx-impl';
import { Backend, Profile } from 'client';
import { Palette } from 'taskdescriptor';
import { useProjectId } from './hooks';

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
  const { projectId } = useProjectId();
  const [loading, setLoading] = React.useState<boolean>(true);
  const [state, setState] = React.useState<TasksState>(init.withProfile(profile));
  const setter: TasksDispatch = React.useCallback((mutator: TasksMutator) => setState(mutator), [setState]);


  const apps = Burger.useApps();
  
  const isTasks = false;

  React.useEffect(() => {
    if(isTasks) {
      console.log("SHOULD RELOAD");
    }
    
  }, [isTasks])


  const contextValue: TasksContextType = React.useMemo(() => {
    return {
      state, setState: setter, loading, palette: Palette, reload: async () => {
        if(!isTasks) {
          return;
        }
        backend.task.getActiveTasks().then(data => {
          setState(prev => prev.withTasks(data.records))
        });
      }
    };
  }, [state, setter, loading, backend, isTasks]);

  React.useEffect(() => {
    if (!loading || !isTasks) {
      return;
    }
    backend.task.getActiveTasks().then(data => {
      setLoading(false);
      setState(prev => prev.withProfile(profile).withTasks(data.records))
    });

  }, [loading, setLoading, backend, profile, isTasks]);

  return (<TasksContext.Provider value={contextValue}>{children}</TasksContext.Provider>);
};


export { TasksProvider, TasksContext };

