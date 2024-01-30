import React from 'react';

import { usePreference, PreferenceProvider, PreferenceInit } from 'descriptor-prefs';
import { TaskDescriptor  } from 'descriptor-task';


export type ColumnName = keyof TaskDescriptor;

export const ColumnNameOptions: ColumnName[] = [
  'title',
  'assignees',
  'dueDate',
  'priority',
  'roles',
  'status'];

function initPrefs(): PreferenceInit {
  return {
    id: 'task-search-page',
    fields: ColumnNameOptions,
    sorting: {
      dataId: 'dueDate',
      direction: 'asc'
    }
  }
}

export const useTaskPrefs = usePreference;

export const TaskPrefsProvider: React.FC<{children: React.ReactNode}> = ({children}) => {
  return (<PreferenceProvider init={initPrefs()}><>{children}</></PreferenceProvider>);
}