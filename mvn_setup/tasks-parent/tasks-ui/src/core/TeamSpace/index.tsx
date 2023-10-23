import React from 'react';
import Context from 'context';

import { TaskListTabState, TaskList } from '../TaskList';

import TaskItem from './TaskItem';
import TaskItemActive from './TaskItemActive';

function groupsToRecord(state: Context.Group[]): Record<Context.TeamGroupType, Context.Group> {
  return state.reduce((acc, item) => ({ ...acc, [item['id']]: item }), {} as Record<Context.TeamGroupType, Context.Group>);
}

function getTabs(state: Context.TasksState): TaskListTabState[] {
  const groupBy: Context.Group[] = state.withGroupBy("team").groups;
  const groups = groupsToRecord(groupBy);
  const groupOverdue = groups["groupOverdue"];
  const groupDueSoon = groups["groupDueSoon"];
  const groupAvailable = groups["groupAvailable"];


  return [
    {
      id: 0,
      label: 'core.teamSpace.tab.task.overdue',
      color: Context.TeamGroupPallete.groupOverdue,
      group: groupOverdue,
      disabled: true,
      count: groupOverdue.records.length
    },
    {
      id: 1,
      label: 'core.teamSpace.tab.task.dueSoon',
      color: Context.TeamGroupPallete.groupDueSoon,
      group: groupDueSoon,
      disabled: true,
      count: groupDueSoon.records.length

    },
    {
      id: 2,
      label: 'core.teamSpace.tab.task.available',
      color: Context.TeamGroupPallete.groupAvailable,
      group: groupAvailable,
      disabled: true,
      count: groupAvailable.records.length
    }
  ]
}


const TeamSpaceLoader: React.FC = () => {
  const tasks = Context.useTasks();

  if (tasks.loading) {
    return <>...loading</>
  }

  return (<TaskList state={getTabs(tasks.state)}>{{ TaskItem, TaskItemActive }}</TaskList>);
}

export default TeamSpaceLoader;