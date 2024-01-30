import React from 'react';


import { TaskDescriptor, TaskSearch } from 'descriptor-task';
import { getInstance as createGroups } from 'descriptor-grouping';
import { ImmutableCollection } from 'descriptor-grouping';
import { GroupByTypes, useSearch } from './TaskSearchContext';


import LoggerFactory from 'logger';
const _logger = LoggerFactory.getLogger();



const defs: Record<GroupByTypes, (task: TaskDescriptor) => any> = {
  none: () => "",
  owners: (task) => task.assignees,
  roles: (task) => task.roles,
  status: (task) => task.status,
  priority: (task) => task.priority,
}

function initCollection(search: TaskSearch, type: GroupByTypes) {

  return new ImmutableCollection<TaskDescriptor>({
    classifierName: type,
    origin: Object.freeze(search.data),
    definition: defs[type],
    groupValues: []
  });
}

const GroupByNone = createGroups<TaskDescriptor>();
const GroupByOwners = createGroups<TaskDescriptor>();
const GroupByRoles = createGroups<TaskDescriptor>();
const GroupByStatus = createGroups<TaskDescriptor>();
const GroupByPriority = createGroups<TaskDescriptor>();


export function useGrouping() {
  const { groupBy } = useSearch();

  const none = GroupByNone.hooks.useGrouping()
  const owner = GroupByOwners.hooks.useGrouping();
  const roles = GroupByRoles.hooks.useGrouping();
  const status = GroupByStatus.hooks.useGrouping();
  const priority = GroupByPriority.hooks.useGrouping();

  if(groupBy === 'owners') {
    return owner;
  } else if(groupBy === 'roles') {
    return roles;
  } else if(groupBy === 'status') {
    return status;
  } else if(groupBy === 'priority') {
    return priority;
  }
  return none; 
}

export const TaskGroupingProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { state, groupBy } = useSearch();
  _logger.target({ groupBy }).debug("loading group");

  if(groupBy === 'owners') {
    return <GroupByOwners.Provider init={initCollection(state, groupBy)}><>{children}</></GroupByOwners.Provider>;
  } else if(groupBy === 'roles') {
    return <GroupByRoles.Provider init={initCollection(state, groupBy)}><>{children}</></GroupByRoles.Provider>;
  } else if(groupBy === 'status') {
    return <GroupByStatus.Provider init={initCollection(state, groupBy)}><>{children}</></GroupByStatus.Provider>;
  } else if(groupBy === 'priority') {
    return <GroupByPriority.Provider init={initCollection(state, groupBy)}><>{children}</></GroupByPriority.Provider>;
  }
  return <GroupByNone.Provider init={initCollection(state, groupBy)}><>{children}</></GroupByNone.Provider>;
}

