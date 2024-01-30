import React from 'react';

import { usePreference, PreferenceProvider, PreferenceInit } from 'descriptor-prefs';
import { TaskDescriptor  } from 'descriptor-task';
import { GroupByOptions, GroupByTypes } from '.';


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
export const PrefConfigFields = {
  groupBy: "groupByField",
  searchBy: "searchByField"
}
export const useTaskPrefs = usePreference;

export function useTaskPrefsInit(): { groupBy: GroupByTypes, searchString: string} {
  const ctx = useTaskPrefs();
  const config = ctx.pref.config;
  const groupBy: GroupByTypes = config.find(({ dataId }) => dataId === PrefConfigFields.groupBy)?.value as any ?? 'status';
  const searchString = config.find(({ dataId }) => dataId === PrefConfigFields.searchBy)?.value ?? '';

  return { groupBy: GroupByOptions.includes(groupBy) ? groupBy : 'status', searchString }
}

export const TaskPrefsProvider: React.FC<{children: React.ReactNode}> = ({children}) => {
  return (<PreferenceProvider init={initPrefs()}><>{children}</></PreferenceProvider>);
}