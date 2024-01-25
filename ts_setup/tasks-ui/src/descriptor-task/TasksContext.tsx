import React from 'react';
import { Backend, UserProfileAndOrg, Task } from 'client';
import { TasksContextType, TaskDescriptor } from './types';
import { ImmutableTaskDescriptor } from './ImmutableTaskDescriptor';


export const TasksContext = React.createContext<TasksContextType>({} as TasksContextType);

type WithTasks = (tasks: Task[]) => void;


function initTasks(tasks: Task[], profile: UserProfileAndOrg): {
  roles: readonly string[], 
  owners: readonly string[], 
  tasks: readonly TaskDescriptor[] 
}  {
  
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  
  const roles: string[] = [];
  const owners: string[] = [];
  
  const next = tasks.map(task => {
    for(const role of task.roles) {
      if(!roles.includes(role)) {
        roles.push(role);
      }
    }
    for(const owner of task.assigneeIds) {
      if(!owners.includes(owner)) {
        owners.push(owner);
      }
    }
    return new ImmutableTaskDescriptor(task, profile, today);
  });

  return { 
    tasks: Object.freeze(next), 
    roles: Object.freeze(roles),
    owners: Object.freeze(owners),
  }
}

export const TasksProvider: React.FC<{ children: React.ReactNode, init: { backend: Backend, profile: UserProfileAndOrg } }> = ({ children, init }) => {
  const { backend, profile } = init;
  const [loading, setLoading] = React.useState<boolean>(true);
  const [tasks, setTasks] = React.useState<readonly TaskDescriptor[]>([]);
  const [roles, setRoles] = React.useState<readonly string[]>([]);
  const [owners, setOwners] = React.useState<readonly string[]>([]);

  const withTasks: WithTasks = React.useCallback((tasks: Task[]) => {
    const next = initTasks(tasks, profile);
    setTasks(next.tasks);
    setRoles(next.roles);
    setOwners(next.owners);
  }, [setTasks, setRoles, profile]);

  const contextValue: TasksContextType = React.useMemo(() => {
    async function reload() {
      return backend.task.getActiveTasks().then(data => withTasks(data.records));
    }
    return { loading, tasks, roles, owners, withTasks, reload };
  }, [loading, backend, tasks, roles, withTasks]);

  React.useEffect(() => {
    if (!loading) {
      return;
    }
    backend.task.getActiveTasks().then(data => {
      withTasks(data.records);
      setLoading(false);
    });

  }, [loading, backend, profile, setLoading, withTasks]);

  return (<TasksContext.Provider value={contextValue}>{children}</TasksContext.Provider>);
}

