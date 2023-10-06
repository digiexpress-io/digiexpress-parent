import React from 'react';
import Client from '@taskclient';

import { TaskListTabState, TaskList } from '../TaskList';

import TaskItem from './TaskItem';
import TaskItemActive from './TaskItemActive';

function groupsToRecord(state: Client.Group[]): Record<Client.TeamGroupType, Client.Group> {
  return state.reduce((acc, item) => ({ ...acc, [item['id']]: item }), {} as Record<Client.TeamGroupType, Client.Group>);
}

function getTabs(state: Client.TasksState): TaskListTabState[] {
  const groupBy: Client.Group[] = state.withGroupBy("team").groups;
  const groups = groupsToRecord(groupBy);
  const groupOverdue = groups["groupOverdue"];
  const groupDueSoon = groups["groupDueSoon"];
  const groupAvailable = groups["groupAvailable"];


  return [
    {
      id: 0,
      label: 'core.teamSpace.tab.task.overdue',
      color: Client.TeamGroupPallete.groupOverdue,
      group: groupOverdue,
      disabled: true,
      count: groupOverdue.records.length
    },
    {
      id: 1,
      label: 'core.teamSpace.tab.task.dueSoon',
      color: Client.TeamGroupPallete.groupDueSoon,
      group: groupDueSoon,
      disabled: true,
      count: groupDueSoon.records.length

    },
    {
      id: 2,
      label: 'core.teamSpace.tab.task.available',
      color: Client.TeamGroupPallete.groupAvailable,
      group: groupAvailable,
      disabled: true,
      count: groupAvailable.records.length
    }
  ]
}


const TeamSpaceLoader: React.FC = () => {
  const tasks = Client.useTasks();

  if (tasks.loading) {
    return <>...loading</>
  }

  return (<TaskList state={getTabs(tasks.state)}>{{ TaskItem, TaskItemActive }}</TaskList>);
}

export default TeamSpaceLoader;