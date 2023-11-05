import React from 'react';
import Context from 'context';
import { Group, AssigneeGroupType } from 'descriptor-task';
import { TaskListTabState, TaskList } from '../TaskList';
import TaskItemActive from './TaskItemActive';
import TaskItem from './TaskItem';
import { TasksState, AssigneePalette } from 'descriptor-task';


function groupsToRecord(state: Group[]): Record<AssigneeGroupType, Group> {
  return state.reduce((acc, item) => ({ ...acc, [item['id']]: item }), {} as Record<AssigneeGroupType, Group>);
}

function getTabs(state: TasksState): TaskListTabState[] | any {
  const groupBy: Group[] = state.withDescriptors().withGroupBy("assignee").groups;
  const groups = groupsToRecord(groupBy);
  const assigneeOverdue = groups["assigneeOverdue"];
  const assigneeOther = groups["assigneeOther"];
  const assigneeStartsToday = groups["assigneeStartsToday"];
  const assigneeCurrentlyWorking = groups["assigneeCurrentlyWorking"];

  return [
    {
      id: 0,
      label: 'core.myWork.tab.task.currentlyWorking',
      color: AssigneePalette.assigneeCurrentlyWorking,
      group: assigneeCurrentlyWorking,
      disabled: true,
      count: assigneeCurrentlyWorking.records.length
    },
    {
      id: 1,
      label: 'core.myWork.tab.task.overdue',
      color: AssigneePalette.assigneeOverdue,
      group: assigneeOverdue,
      disabled: true,
      count: assigneeOverdue.records.length
    },
    {
      id: 2,
      label: 'core.myWork.tab.task.startsToday',
      color: AssigneePalette.assigneeStartsToday,
      group: assigneeStartsToday,
      disabled: true,
      count: assigneeStartsToday.records.length
    },
    {
      id: 3,
      label: 'core.myWork.tab.task.available',
      color: AssigneePalette.assigneeOther,
      group: assigneeOther,
      disabled: true,
      count: assigneeOther.records.length
    },
    {
      id: 4,
      label: 'core.myWork.tab.recentActivities',
      color: '#A1A314',
      group: assigneeOther,
      disabled: true,
      count: assigneeOther.records.length
    }
  ]
}

const MyWorkLoader: React.FC = () => {
  const tasks = Context.useTasks();
  console.log(getTabs(tasks.state));

  if (tasks.loading) {
    return <>...loading</>
  }
  return (<TaskList state={getTabs(tasks.state)}>{{ TaskItem, TaskItemActive }}</TaskList>);
}

export default MyWorkLoader;