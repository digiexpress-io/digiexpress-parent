import React from 'react';

import { Backend } from 'client';
import { UserProfileAndOrg } from 'descriptor-user-profile';

import { Task, TaskUpdateCommand, TaskId, CreateTask } from './backend-types';
import { TasksContextType, TaskDescriptor } from './descriptor-types';
import { ImmutableTaskDescriptor } from './ImmutableTaskDescriptor';
import { ImmutableTaskStore } from './ImmutableTaskStore';


export const TasksContext = React.createContext<TasksContextType>({} as TasksContextType);

type WithTasks = (tasks: Task[]) => void;


function initTasks(tasks: Task[], profile: UserProfileAndOrg): {
  roles: readonly string[], 
  owners: readonly string[], 
  tasksById: Record<string, TaskDescriptor>;
  tasks: readonly TaskDescriptor[];
}  {
  
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  
  const roles: string[] = [];
  const owners: string[] = [];
  const tasksById: Record<string, TaskDescriptor> = {};
  
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
    const result = new ImmutableTaskDescriptor(task, profile, today);
    tasksById[result.id] = result;
    return result;
  });

  return { 
    tasks: Object.freeze(next), 
    roles: Object.freeze(roles),
    owners: Object.freeze(owners),
    tasksById
  }
}

export const TasksProvider: React.FC<{ children: React.ReactNode, init: { backend: Backend, profile: UserProfileAndOrg } }> = ({ children, init }) => {
  const { backend, profile } = init;

  
  const [loading, setLoading] = React.useState<boolean>(true);
  const [tasks, setTasks] = React.useState<Record<string, TaskDescriptor>>({});
  const [roles, setRoles] = React.useState<readonly string[]>([]);
  const [owners, setOwners] = React.useState<readonly string[]>([]);
  const [store] = React.useState(new ImmutableTaskStore(backend.store));



  const withTasks: WithTasks = React.useCallback((tasks: Task[]) => {
    const next = initTasks(tasks, profile);
    setTasks(next.tasksById);
    setRoles(next.roles);
    setOwners(next.owners);
  }, [setTasks, setRoles, profile]);

  const contextValue: TasksContextType = React.useMemo(() => {
    
    async function createTask(command: CreateTask): Promise<Task> {
      const task = store.createTask(command);
      await store.getActiveTasks().then(data => withTasks(data.records));
      return task;
    }

    async function reload() {
      return store.getActiveTasks().then(data => withTasks(data.records));
    }

    async function updateActiveTask(id: TaskId, commands: TaskUpdateCommand<any>[]) {
      const task = await store.updateActiveTask(id, commands);
      await store.getActiveTasks().then(data => withTasks(data.records));
      return task;
    }

    function getById(id: string) { return tasks[id]; }
    const allTasks = Object.freeze(Object.values(tasks));
    return { loading, tasks: allTasks, roles, owners, withTasks, reload, getById, store, updateActiveTask, createTask };
  }, [loading, store, tasks, roles, withTasks,]);

  React.useEffect(() => {
    if (!loading) {
      return;
    }

    store.getActiveTasks().then(data => {
      withTasks(data.records);
      setLoading(false);
    });

  }, [loading, store, profile, setLoading, withTasks]);

  return (<TasksContext.Provider value={contextValue}>{children}</TasksContext.Provider>);
}

