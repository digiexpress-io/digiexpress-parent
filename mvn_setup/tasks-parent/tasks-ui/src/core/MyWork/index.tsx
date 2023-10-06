import React from 'react';

import Client from '@taskclient';

import { TaskListTabState, TaskList } from '../TaskList';
import TaskItemActive from './TaskItemActive';
import TaskItem from './TaskItem';



function groupsToRecord(state: Client.Group[]): Record<Client.AssigneeGroupType, Client.Group> {
  return state.reduce((acc, item) => ({ ...acc, [item['id']]: item }), {} as Record<Client.AssigneeGroupType, Client.Group>);
}


function getTabs(state: Client.TasksState): TaskListTabState[] {
  const groupBy: Client.Group[] = state.withGroupBy("assignee").groups;
  const groups = groupsToRecord(groupBy);
  const assigneeOverdue = groups["assigneeOverdue"];
  const assigneeOther = groups["assigneeOther"];
  const assigneeStartsToday = groups["assigneeStartsToday"];
  const assigneeCurrentlyWorking = groups["assigneeCurrentlyWorking"];


  return [
    {
      id: 0,
      label: 'core.myWork.tab.task.currentlyWorking',
      color: Client.AssigneePalette.assigneeCurrentlyWorking,
      group: assigneeCurrentlyWorking,
      disabled: true,
      count: assigneeCurrentlyWorking.records.length
    },
    {
      id: 1,
      label: 'core.myWork.tab.task.overdue',
      color: Client.AssigneePalette.assigneeOverdue,
      group: assigneeOverdue,
      disabled: true,
      count: assigneeOverdue.records.length
    },
    {
      id: 2,
      label: 'core.myWork.tab.task.startsToday',
      color: Client.AssigneePalette.assigneeStartsToday,
      group: assigneeStartsToday,
      disabled: true,
      count: assigneeStartsToday.records.length
    },
    {
      id: 3,
      label: 'core.myWork.tab.task.available',
      color: Client.AssigneePalette.assigneeOther,
      group: assigneeOther,
      disabled: true,
      count: assigneeOther.records.length
    }
  ]
}

const MyWorkLoader: React.FC = () => {
  const tasks = Client.useTasks();


  if (tasks.loading) {
    return <>...loading</>
  }


  return (<TaskList state={getTabs(tasks.state)}>{{ TaskItem, TaskItemActive }}</TaskList>);
}

export default MyWorkLoader;