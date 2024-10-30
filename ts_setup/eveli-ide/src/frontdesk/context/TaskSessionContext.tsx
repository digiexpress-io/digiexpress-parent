import React, { createContext, PropsWithChildren, useContext, useState } from 'react'
import { SessionRefreshContext } from './SessionRefreshContext'
import { QueryResult } from '@material-table/core'
import { ROLE_AUTHORIZED } from '../util/rolemapper'
import { useSnackbar } from 'notistack';
import { useIntl } from 'react-intl'
import { useUserInfo } from './UserContext'
import { TaskApiConfig, TaskApiConfigContext, TaskBackend, TaskBackendProvider } from './TaskApiConfigContext';
import { Task } from '../types/task/Task';
import { Comment, CommentSource } from '../types/task/Comment';

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

export const TaskSessionContext:React.FC<PropsWithChildren<TaskApiConfig>> = ({apiBaseUrl, children}) => {

  console.log('app base url', apiBaseUrl)

  const session = useContext(SessionRefreshContext);
  const { enqueueSnackbar } = useSnackbar();
  const intl = useIntl();
  const userInfo = useUserInfo();

  const [filters, setFilters] = useState<any>();
  const [sort, setSort] = useState<any>();
  const [paging, setPaging] = useState<any>();

  const getTasks = (page=0, size=20):Promise<QueryResult<Task>> => {
    return session.cFetch(`${apiBaseUrl}/task?page=${page}&size=${size}`)
      .then(response => response.json())
      .then(json=>{
        return {
          data: json._embedded?.tasks || [],
          page: json.page.number,
          totalCount: json.page.totalElements
        };
      });
  }

  const getTask = (taskId:any) => {
    return session.cFetch(`${apiBaseUrl}/task/${taskId}`)
    .then(response => response.json())
    .then(task => {
      if (task.dueDate) {
        task.dueDate = new Date(task.dueDate);
      }
      return task;
    });
  }
  const saveTask = (task:Task) => {
    let method = 'POST';
    let url = `${apiBaseUrl}/task/`;
    if (task.id) {
      method = 'PUT';
      url = url + task.id;
    }
    else {
      // default label for created task
      if (!(task.keyWords && task.keyWords?.length >0)) {
        task.keyWords = ['Manual'];
      }
      // by default visible to all users
      if (!task.assignedRoles) {
        task.assignedRoles = [ROLE_AUTHORIZED];
      }
    }
    return session.cFetch(url, {method: method,
      body: JSON.stringify(task)
    })
    .then(response => response.json());
  }
  const deleteTask = (taskId:any) => {
    return session.cFetch(`${apiBaseUrl}/task/${taskId}`, 
      {method: 'DELETE'})
    .then(response=>{
      if (!response.ok) {
        let message = 'error.dataAccess';
        if (response.status === 403) {
          message = 'error.unauthorizedAccess';
        }
        enqueueSnackbar(intl.formatMessage({id: message}), {variant: 'error'});
      }
      return response;
    });
  }
  const getTaskComments = (task:Task) => {
    if (!task.id) {
      return Promise.resolve();
    }
    const commentsUrl = `${apiBaseUrl}/task/${task.id}/comments`;
    
    return session.cFetch(commentsUrl)
    .then(response => {
      if (response.ok) return response.json();
      throw new Error("Error with code:" + response.status);
    });
  }

  const saveComment = (commentText:string, replyToId:number|undefined, task:Task, isExternalThread:boolean|undefined):Promise<Comment> => {
    let savingComment = {
      commentText: commentText,
      replyToId: replyToId,
      taskId: task.id,
      external: isExternalThread,
      userName: userInfo.user.name,
      source: CommentSource.FRONTDESK
    };
    let url = `${apiBaseUrl}/comment`;
    return session.cFetch(url, {method: 'POST',
      body: JSON.stringify(savingComment)
    })
    .then(response => {
      if (response.ok) return response.json();
      throw new Error("Comment save error:" + response.status);
    })
    .then((comment:Comment) => {
      return comment;
    });
  }

  const apiSessionContext:TaskBackend = {
    getTasks: getTasks,
    getTask: getTask,
    saveTask: saveTask,
    deleteTask: deleteTask,
    getTaskComments: getTaskComments,
    saveComment: saveComment
  }

  const tableState:TableState = {
    filters,
    setFilters,
    sort,
    setSort,
    paging,
    setPaging
  }

  return (
    <TaskApiConfigContext.Provider value={{apiBaseUrl}}>
      <TaskBackendProvider value={apiSessionContext}>
        <TableStateContext.Provider value={tableState}>
          {children}
        </TableStateContext.Provider>
      </TaskBackendProvider>
    </TaskApiConfigContext.Provider>
  )
}