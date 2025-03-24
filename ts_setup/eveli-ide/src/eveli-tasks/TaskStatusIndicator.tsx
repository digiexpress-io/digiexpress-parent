import React from 'react';
import { FormattedMessage } from 'react-intl';
import { TaskApi } from '../api-task';
import { TaskIndicator } from './TaskIndicator';


export const TaskStatusIndicator: React.FC<{
  value: keyof typeof TaskApi.TaskStatus | undefined
}> = ({ value })=> {
  if (!value) {
    return null;
  }
  const color = TaskApi.task_status_colors[value];
  return (
    <TaskIndicator color={color ? color : undefined} withLabel>
      <FormattedMessage {...TaskApi.task_status_messages[value]}/>
    </TaskIndicator>
  );
}