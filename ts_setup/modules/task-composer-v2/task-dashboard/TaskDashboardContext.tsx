import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';

import React from 'react';




interface TaskDashboardContextType {
  task: TaskApi.Task,
  saveTask(changes: Partial<TaskApi.Task>): Promise<TaskApi.Task>;
  saveCustomerComment(changes: { commentText: string }): Promise<TaskApi.Task>;
  saveTaskNote(changes: { commentText: string }): Promise<TaskApi.Task>;

  isTaskChanged(changes: Partial<TaskApi.Task>): boolean;
}



const TaskDashboardContext = React.createContext({} as any);

const TaskDashboardContextProvider: React.FC<{ taskId: string, children: React.ReactElement }> = (props) => {
  const [task, setTask] = React.useState<TaskApi.Task>();
  const backend = useTaskBackend();

  React.useEffect(() => {
    if (props.taskId && task === undefined) {
      backend.persistence.getOneTask(props.taskId).then(setTask);
    }
  }, [props.taskId, task]);


  const contextValue: TaskDashboardContextType = React.useMemo(() => {
    async function saveTask(changes: Partial<TaskApi.Task>): Promise<TaskApi.Task> {
      const start = task!;
      const mutations: Partial<TaskApi.Task> = {
        id: start.id,
        version: start.version,
        keyWords: start.keyWords,
        priority: start.priority,
        subject: start.subject,
        description: start.description,
        dueDate: start.dueDate as Date | undefined,
        status: start.status,
        assignedUser: start.assignedUser,
        assignedUserEmail: start.assignedUserEmail,
        clientIdentificator: start.clientIdentificator,
        assignedRoles: start.assignedRoles,
        additionalInfo: start.additionalInfo,
        ...changes
      };
      await backend.persistence.modifyOneTask(mutations as TaskApi.Task);
      const next = await backend.persistence.getOneTask(start.id);
      setTask(next);
      console.log(mutations)
      return next;
    }

    function isTaskChanged(changes: Partial<TaskApi.Task>): boolean {
      const start: any = task!;
      return Object.entries(changes).filter(([name, value]) => {
        const previous: any = start[name];
        return previous !== value;
      }).length > 0;
    }
    
    async function saveCustomerComment(changes: { commentText: string }): Promise<TaskApi.Task> {
      const start = task!;
      await backend.persistence.createOneComment(changes.commentText, undefined, start, true);
      const next = await backend.persistence.getOneTask(start.id);
      setTask(next);
      return next;
    }

    // internal comments
    async function saveTaskNote(changes: { commentText: string }): Promise<TaskApi.Task> {
      const start = task!;
      await backend.persistence.createOneComment(changes.commentText, undefined, start, false);
      const next = await backend.persistence.getOneTask(start.id);
      setTask(next);
      return next;
    }


    return { task: task ?? {} as any, saveTask, isTaskChanged, saveCustomerComment, saveTaskNote }

  }, [task]);

  return (<TaskDashboardContext.Provider value={contextValue}>{task && props.children}</TaskDashboardContext.Provider>)
}

const useTaskDashboard = () => {
  const ctx: TaskDashboardContextType = React.useContext(TaskDashboardContext);
  return ctx;
}

export { TaskDashboardContext, TaskDashboardContextProvider, useTaskDashboard }



