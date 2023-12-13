import React from 'react';
import { Backend, UserProfileAndOrg } from 'client';
import { TasksContextType, TasksMutator, TasksDispatch, TasksState } from './types';
import { TasksStateBuilder } from './context-tasks-state';
import { Palette } from './constants';


const TasksContext = React.createContext<TasksContextType>({} as TasksContextType);

const initState: TasksState = new TasksStateBuilder({
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
  profile: {
    user: {
      created: new Date(),
      updated: new Date(),
      details: {
        username: '',
        email: '',
        firstName: '',
        lastName: ''
      },
      notificationSettings: [{
        type: '',
        enabled: true
      }]
    },
    userId: "", today: new Date(), roles: []
  }
});

const TasksProvider: React.FC<{ children: React.ReactNode, init: { backend: Backend, profile: UserProfileAndOrg } }> = ({ children, init }) => {
  const { backend, profile } = init;
  const [loading, setLoading] = React.useState<boolean>(true);

  const [state, setState] = React.useState<TasksState>(initState.withProfile(profile));
  const setter: TasksDispatch = React.useCallback((mutator: TasksMutator) => setState(mutator), [setState]);
  const contextValue: TasksContextType = React.useMemo(() => {
    return {
      state, setState: setter, loading, palette: Palette, reload: async () => {
        return backend.task.getActiveTasks().then(data => {
          return setState(prev => prev.withTasks(data.records))
        });
      }
    };
  }, [state, setter, loading, backend]);

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

