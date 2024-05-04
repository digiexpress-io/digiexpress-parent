import React from 'react';


import { TaskDescriptor, TaskSearch, ImmutableTaskSearch, TaskStatus, TaskPriority, useTasks } from 'descriptor-task';

import { useTaskPrefsInit } from './TaskPrefsContext';


export type GroupByTypes = 'none'| 'owners'| 'roles'| 'status'| 'priority';
export const GroupByOptions: GroupByTypes[] = ['none', 'owners', 'roles', 'status', 'priority'];

export interface TaskSearchContextType {
  state: TaskSearch;
  groupBy: GroupByTypes;
  withGrouBy(groupBy: GroupByTypes): void;

  withData(input: readonly TaskDescriptor[]): void;
  withSearchString(searchString: string): void;
  withFilterByStatus(status: TaskStatus[]): void
  withFilterByPriority(priority: TaskPriority[]): void
  withFilterByOwner(owners: string[]): void
  withFilterByRoles(roles: string[]): void
  withoutFilters(): void;
}


type SetData = React.Dispatch<React.SetStateAction<ImmutableTaskSearch>>;
type SetGrouBy = React.Dispatch<React.SetStateAction<GroupByTypes>>;
type WithGroupBy = (input: GroupByTypes) => void;
type WithData = (input: readonly TaskDescriptor[]) => void;
type WithSearchString = (searchString: string) => void;
type WithFilterByStatus = (status: TaskStatus[]) => void;
type WithFilterByPriority = (priority: TaskPriority[]) => void;
type WithFilterByOwner = (owners: string[]) => void;
type WithFilterByRoles = (roles: string[]) => void;
type WithoutFilters = () => void;


function initWithGroupBy(input: GroupByTypes, setData: SetData, setGroupBy: SetGrouBy) {
  setGroupBy(input);
}
function initWithData(input: readonly TaskDescriptor[], setData: SetData) {
  setData(current => current.withData(input));
}
function initWithSearchString(searchString: string, setData: SetData) {
  setData(current => current.withSearchString(searchString));
}
function initWithFilterByStatus(status: TaskStatus[], setData: SetData) {
  setData(current => current.withFilterByStatus(status));
}
function initWithFilterByPriority(priority: TaskPriority[], setData: SetData) {
  setData(current => current.withFilterByPriority(priority));
}
function initWithFilterByOwner(owners: string[], setData: SetData) {
  setData(current => current.withFilterByOwner(owners));
}
function initWithFilterByRoles(roles: string[], setData: SetData) {
  setData(current => current.withFilterByRoles(roles));
}
function initWithoutFilters(setData: SetData) {
  setData(current => current.withoutFilters());
}


export const TaskSearchContext = React.createContext<TaskSearchContextType>({} as any);

export function useSearch() {
  const result: TaskSearchContextType = React.useContext(TaskSearchContext);
  return result;
}

/**
 *  DELEGATE for loading all the contexts after first task loading...
 */
const TaskSearchDelegate: React.FC<{ children: React.ReactNode, init: { groupBy: GroupByTypes, searchString: string}}> = ({children, init}) => {
  const ctx = useTasks();
  const { tasks } = ctx;
  const [groupBy, setGroupBy] = React.useState<GroupByTypes>(init.groupBy);
  const [state, setState] = React.useState<ImmutableTaskSearch>(new ImmutableTaskSearch({ data: [], searchString: init.searchString }));

  const withGrouBy: WithGroupBy = React.useCallback((groupBy) => initWithGroupBy(groupBy, setState, setGroupBy), [setState, setGroupBy]);

  const withData: WithData = React.useCallback((data) => initWithData(data, setState), [setState, setGroupBy]);
  const withSearchString: WithSearchString = React.useCallback((searchString) => initWithSearchString(searchString, setState), [setState, setGroupBy]);
  const withFilterByStatus: WithFilterByStatus = React.useCallback((status) => initWithFilterByStatus(status, setState), [setState, setGroupBy]);

  const withFilterByPriority: WithFilterByPriority = React.useCallback((priority) => initWithFilterByPriority(priority, setState), [setState, setGroupBy]);
  const withFilterByOwner: WithFilterByOwner = React.useCallback((owner) => initWithFilterByOwner(owner, setState), [setState, setGroupBy]);
  const withFilterByRoles: WithFilterByRoles = React.useCallback((roles) => initWithFilterByRoles(roles, setState), [setState, setGroupBy]);
  const withoutFilters: WithoutFilters = React.useCallback(() => initWithoutFilters(setState), [setState]);

  const contextValue: TaskSearchContextType = React.useMemo(() => {
    return { 
      groupBy, state, withGrouBy,
      withData, withSearchString, 
      withFilterByStatus, withFilterByPriority, 
      withFilterByOwner, withFilterByRoles, withoutFilters};
  }, [
    groupBy, state, withGrouBy,
    withData, withSearchString, 
    withFilterByStatus, withFilterByPriority, 
    withFilterByOwner, withFilterByRoles, withoutFilters]);

  return (<TaskSearchContext.Provider value={contextValue}><>{children}</></TaskSearchContext.Provider>);
}


/**
 * Put all the contexts together with initial task loading
 */
export const TaskSearchProvider: React.FC<{children: React.ReactNode}> = ({children}) => {
  const taskCtx = useTasks();
  const prefsInit = useTaskPrefsInit();

  if (taskCtx.loading) {
    return <>...loading</>
  }

  return (<TaskSearchDelegate init={prefsInit}><>{children}</></TaskSearchDelegate>);
}