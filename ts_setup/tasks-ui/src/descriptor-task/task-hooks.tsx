import React from 'react';
import { TaskEditContextType, TasksContextType } from './descriptor-types';
import { TaskEditContext } from './TaskEditContext';
import { TasksContext } from './TasksContext';


export const useTaskEdit = () => {
  const result: TaskEditContextType = React.useContext(TaskEditContext);
  return result;
}

export const useTasks = () => {
  const result: TasksContextType = React.useContext(TasksContext);
  return result;
}