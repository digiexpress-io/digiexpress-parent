import React, { createContext, useState } from 'react'

import { TaskBackend, TaskBackendProvider } from './TaskApiConfigContext';
import { Task } from '../types/task/Task';
import { useFetch } from '@dxs-ts/eveli-fetch';

export interface TableState {
  sort: any;
  setSort:(sort:any)=>void;
  filters: any;
  setFilters: (filter:any)=>void;
  paging: any;
  setPaging: (paging:any)=>void;
}

export const TableStateContext = createContext<TableState>({
  sort:undefined, setSort:()=>{}, 
  filters:undefined, setFilters:()=>{},
  paging:undefined, setPaging: ()=>{}});

export const TaskSessionContext: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { getTasks } = useFetch('worker/rest/api/tasks.GET', {});
  const { getTask } = useFetch('worker/rest/api/tasks/$taskId.GET', {});
  const { updateTask } = useFetch('worker/rest/api/tasks/$taskId.PUT', {});
  const { deleteTask } = useFetch('worker/rest/api/tasks/$taskId.DELETE', {});
  const { createTask } = useFetch('worker/rest/api/tasks.POST', {});
  const { getTaskComments } = useFetch('worker/rest/api/tasks/$taskId/comments.GET', {});
  const { saveComment } = useFetch('worker/rest/api/tasks/$taskId/comments.POST', {});

  const [filters, setFilters] = useState<any>();
  const [sort, setSort] = useState<any>();
  const [paging, setPaging] = useState<any>();

  function saveTask(task: Task) {
    if (task.id) {
      return updateTask(task);
    } 
    return createTask(task)
  }

  const apiSessionContext:TaskBackend = { getTasks, getTask, saveTask, deleteTask, getTaskComments, saveComment }

  const tableState: TableState = {
    filters,
    setFilters,
    sort,
    setSort,
    paging,
    setPaging
  }

  return (
    <TaskBackendProvider value={apiSessionContext}>
      <TableStateContext.Provider value={tableState}>
        {children}
      </TableStateContext.Provider>
    </TaskBackendProvider>
  )
}