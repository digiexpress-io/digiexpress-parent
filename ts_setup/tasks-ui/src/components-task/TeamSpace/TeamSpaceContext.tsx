import React from 'react';

import Table from 'table';

import { TaskDescriptor, TeamGroupType, useTasks } from 'descriptor-task';
import { SingleTabInit } from 'descriptor-tabbing';
import { ImmutableCollection, GroupingContextType } from 'descriptor-grouping';

import { Grouping, Tabbing, TaskPagination, TabTypes, } from './teamspace-types';

import LoggerFactory from 'logger';

const _logger = LoggerFactory.getLogger();


function initTable(id: TeamGroupType, grouping: GroupingContextType<TaskDescriptor>): TaskPagination {
  const group = grouping.getByGroupId(id);
  const records = group?.value.map(index => grouping.collection.origin[index]);

  return new Table.TablePaginationImpl<TaskDescriptor>({
    src: records ?? [],
    orderBy: 'dueDate',
    order: 'asc',
    sorted: true,
    rowsPerPage: 15,
  })
}

export function useTeamSpace() {
  const tasks = useTasks();
  const tabbing = Tabbing.hooks.useTabbing();
  const grouping = Grouping.hooks.useGrouping();
  const activeTab = tabbing.getActiveTab();
  const activeTask: TaskDescriptor | undefined = (
    activeTab?.selected && activeTab?.selected.length > 0 ? 
    tasks.getById(activeTab?.selected[0] as string) : undefined);

  function getTabItemCount(tabId: TabTypes) {
    return grouping.getByGroupCount(tabId);
  }

  function setActiveTab(tabId: TabTypes) {
    tabbing.withTabActivity(tabId);
  }

  function setTabPageNo(_garbageEvent: any, newPage: number) {
    tabbing.withTabBody(activeTab.id, (state) => state.withPage(newPage));
  }

  function setTabRowsPerPage(event: React.ChangeEvent<HTMLInputElement>) {
    tabbing.withTabBody(activeTab.id, (state) => state.withRowsPerPage(parseInt(event.target.value, 10)))
  }

  function setActiveTask(task: TaskDescriptor) {
    tabbing.withTabSelecion(activeTab.id, task.id);
  }

  function setActiveTable(callback: (pagination: TaskPagination) => TaskPagination) {
    tabbing.withTabBody(activeTab.id, callback);
  }

  return { 
    activeTab, 
    activeTask,
    setActiveTab, getTabItemCount, 
    setTabPageNo, setTabRowsPerPage,
    setActiveTask,

    setTable: setActiveTable,
    table: activeTab.body };
}

export const ContextReloader: React.FC<{children: React.ReactNode}> = ({children}) => {
  const ctx = useTasks();
  const grouping = Grouping.hooks.useGrouping();
  const tabbing = Tabbing.hooks.useTabbing();

  const { tasks } = ctx;
  const { collection } = grouping;

  React.useEffect(() => {
    _logger.debug("refreshing group data");
    grouping.reducer.withData(tasks);
  }, [tasks]);

  React.useEffect(() => {

    for(const { id } of grouping.collection.groups) {
      _logger.debug("refreshing tab data", id);
      const group = grouping.getByGroupId(id === 'recentActivities' ? "assigneeOther": id);
      const records = group?.value.map(index => grouping.collection.origin[index]) ?? [];
      tabbing.withTabBody(id as TabTypes, (prev) => prev.withSrc(records));
    }
  }, [collection]);

  return (<>{children}</>);
}


export const TeamSpaceTabbing: React.FC<{children: React.ReactNode}> = ({children}) => {
  const grouping = Grouping.hooks.useGrouping();

  function initTabs(): Record<TabTypes, SingleTabInit<TaskPagination>> {
    const result = {
      groupAvailable: { body: initTable("groupAvailable", grouping), active: false  },
      groupDueSoon:   { body: initTable("groupDueSoon", grouping), active: false },
      groupOverdue:   { body: initTable("groupOverdue", grouping), active: true }
    };
  
    //Object.values(result).find(v => v.body.entries.length > 0);
    return result;
  }

  return (
    <Tabbing.Provider init={initTabs()}>
      <>{children}</>
    </Tabbing.Provider>
  );
}

export const TeamSpaceProvider: React.FC<{children: React.ReactNode}> = ({children}) => {
  const ctx = useTasks();

  if (ctx.loading) {
    return <>...loading</>
  }

  function initCollection() {
    const tasks: readonly TaskDescriptor[] = ctx.tasks;
    return new ImmutableCollection<TaskDescriptor>({
      classifierName: "assignee",
      origin: Object.freeze(tasks),
      definition: (task) => task.teamGroupType,
      groupValues: [
        'groupAvailable', 
        'groupDueSoon',
        'groupOverdue']
    });
  }

  return (
    <Grouping.Provider init={initCollection()}>
      <TeamSpaceTabbing>
        <ContextReloader>{children}</ContextReloader>      
      </TeamSpaceTabbing>
    </Grouping.Provider>);
}