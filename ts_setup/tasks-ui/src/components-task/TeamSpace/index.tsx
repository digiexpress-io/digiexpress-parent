import React from 'react';

import Context from 'context';
import { Group, TeamGroupType, TasksState, TeamGroupPalette } from 'descriptor-task';

import { TaskListTabState, TaskList } from '../TaskList';
import TaskItem from './TaskItem';
import TaskItemActive from './TaskItemActive';

function groupsToRecord(state: Group[]): Record<TeamGroupType, Group> {
  return state.reduce((acc, item) => ({ ...acc, [item['id']]: item }), {} as Record<TeamGroupType, Group>);
}

function getTabs(state: TasksState): TaskListTabState[] {
  const groupBy: Group[] = state.toGroupsAndFilters().withGroupBy("team").groups;
  const groups = groupsToRecord(groupBy);
  const groupOverdue = groups["groupOverdue"];
  const groupDueSoon = groups["groupDueSoon"];
  const groupAvailable = groups["groupAvailable"];


  return [
    {
      id: 0,
      label: 'core.teamSpace.tab.task.overdue',
      color: TeamGroupPalette.groupOverdue,
      group: groupOverdue,
      selected: false,
      count: groupOverdue.records.length
    },
    {
      id: 1,
      label: 'core.teamSpace.tab.task.dueSoon',
      color: TeamGroupPalette.groupDueSoon,
      group: groupDueSoon,
      selected: false,
      count: groupDueSoon.records.length

    },
    {
      id: 2,
      label: 'core.teamSpace.tab.task.available',
      color: TeamGroupPalette.groupAvailable,
      group: groupAvailable,
      selected: false,
      count: groupAvailable.records.length
    }
  ]
}


const TeamSpaceLoader: React.FC = () => {
  const tasks = Context.useTasks();

  if (tasks.loading) {
    return <>...loading</>
  }
  const tabs = getTabs(tasks.state);
  return (<TaskList state={tabs}>{{ TaskItem, TaskItemActive }}</TaskList>);
}

export default TeamSpaceLoader;