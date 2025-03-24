import React from 'react';
import { FormattedMessage } from 'react-intl';
import { TaskIndicator } from './TaskIndicator';
import { TaskApi } from '@/api-task'



export const TaskPriorityIndicator: React.FC<{
  value: keyof typeof TaskApi.TaskPriority | undefined
}> = ({ value }) => {
  if (!value) {
    return null;
  }
  const messageKey: keyof typeof TaskApi.TaskPriority | undefined = value;
  return (
    <TaskIndicator color={TaskApi.task_priority_colors[value]} withLabel>
      <FormattedMessage {...TaskApi.task_priority_messages[messageKey]}/>
    </TaskIndicator>
  );
}
