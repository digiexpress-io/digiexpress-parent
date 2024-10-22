import React from 'react'
import { QueryResult } from '@material-table/core';
import { Task } from '../types/task/Task';
import { Comment } from '../types/task/Comment';

export interface TaskApiConfig {
  apiBaseUrl: string
}

export interface TaskBackend {
  getTasks:(page?:number, size?:number)=>Promise<QueryResult<Task>>
  getTask(taskId:any):Promise<Task>
  saveTask(task:Task):Promise<Task>
  deleteTask(taskId:any):Promise<void|Response>
  getTaskComments(task:Task):Promise<Comment[]>
  saveComment(commentText:string, replyToId:number|undefined, task:Task,isExternalThread:boolean|undefined):Promise<Comment>
}

export const TaskApiConfigContext = React.createContext<TaskApiConfig>({
  apiBaseUrl: ''
});

export const getTasksProto = (page?:number, size?:number)=>new Promise<QueryResult<Task>>((resolve,reject)=>reject());
export const getTaskProto = (taskId:any)=>new Promise<Task>((resolve, reject)=>resolve({}));
export const saveTaskProto = (task:Task)=>new Promise<Task>((resolve, reject)=>resolve({}));
export const deleteTaskProto = (taskId:any)=>Promise.resolve();
export const getTaskCommentsProto = (task:Task)=>new Promise<Comment[]>((resolve, reject)=>resolve([]));
export const saveCommentProto = (commentText:string, replyToId:number|undefined, task:Task, isExternalThread:boolean|undefined)=>new Promise<Comment>((resolve, reject)=>reject());

/*
Empty context for defining context.
Also can be used in tests and storybooks: create copy of it and replace needed methods.
*/
export const TaskBackendProto = {
  getTasks: getTasksProto,
  getTask: getTaskProto,
  saveTask: saveTaskProto,
  deleteTask: deleteTaskProto,
  getTaskComments: getTaskCommentsProto,
  saveComment: saveCommentProto
}

export const TaskBackendContext = React.createContext<TaskBackend>(TaskBackendProto);
export const TaskBackendProvider = TaskBackendContext.Provider;
export const TaskBackendConsumer = TaskBackendContext.Consumer;

