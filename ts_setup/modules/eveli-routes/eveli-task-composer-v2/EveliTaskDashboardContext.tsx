import { TaskApi } from '@dxs-ts/eveli-api';
import { useFetch } from '@dxs-ts/envir-fetch';
import React from 'react';




interface EveliTaskDashboardContextType {
  task: TaskApi.Task,
  saveTask(changes: Partial<TaskApi.Task>): Promise<TaskApi.Task>;
  saveCustomerComment(changes: { commentText: string }): Promise<TaskApi.Task>;
  saveTaskNote(changes: { commentText: string }): Promise<TaskApi.Task>;

  isTaskChanged(changes: Partial<TaskApi.Task>): boolean;
}



const EveliTaskDashboardContext = React.createContext({} as any);

const EveliTaskDashboardContextProvider: React.FC<{ taskId: string, children: React.ReactElement }> = (props) => {
  const [task, setTask] = React.useState<TaskApi.Task>();
  const { updateTask } = useFetch('worker/rest/api/tasks/$taskId.PUT', {});
  const { getTask } = useFetch('worker/rest/api/tasks/$taskId.GET', {});
  const { saveComment } = useFetch('worker/rest/api/tasks/$taskId/comments.POST', {});

  React.useEffect(() => {
    if (props.taskId && task === undefined) {
      getTask(props.taskId).then(setTask);
    }
  }, [props.taskId, task]);


  const contextValue: EveliTaskDashboardContextType = React.useMemo(() => {
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
      await updateTask(mutations);
      const next = await getTask(start.id);
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
      await saveComment(changes.commentText, undefined, start, true);
      const next = await getTask(start.id);
      setTask(next);
      return next;
    }

    // internal comments
    async function saveTaskNote(changes: { commentText: string }): Promise<TaskApi.Task> {
      const start = task!;
      await saveComment(changes.commentText, undefined, start, false);
      const next = await getTask(start.id);
      setTask(next);
      return next;
    }


    return { task: task ?? {} as any, saveTask, isTaskChanged, saveCustomerComment, saveTaskNote }

  }, [task, updateTask]);

  return (<EveliTaskDashboardContext.Provider value={contextValue}>{task && props.children}</EveliTaskDashboardContext.Provider>)
}

const useTaskDashboard = () => {
  const ctx: EveliTaskDashboardContextType = React.useContext(EveliTaskDashboardContext);
  return ctx;
}

export { EveliTaskDashboardContext, EveliTaskDashboardContextProvider, useTaskDashboard }



