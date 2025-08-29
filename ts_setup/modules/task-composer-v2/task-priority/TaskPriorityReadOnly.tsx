import React from 'react';
import { Chip, generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';

import { TaskApi } from '@dxs-ts/task-api';
import { useTaskDashboard } from '../task-dashboard';


type Priority = TaskApi.TaskPriority;

const getPriorityColor = (priority: Priority): string => {
  const colorEnum = TaskApi.task_priority_colors[priority];
  switch (colorEnum) {
    case TaskApi.Colors.RED:
      return '#f44336';
    case TaskApi.Colors.BLUE:
      return '#2196f3';
    case TaskApi.Colors.GREEN:
      return '#4caf50';
    default:
      return '#ccc';
  }
};


const getContrastText = (hex: string): string => {
  const c = hex.substring(1);
  const rgb = parseInt(c, 16);
  const r = (rgb >> 16) & 0xff;
  const g = (rgb >> 8) & 0xff;
  const b = rgb & 0xff;

  const luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
  return luminance > 0.6 ? '#000000' : '#ffffff';
};

export const TaskPriorityReadOnly: React.FC = () => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const { task } = useTaskDashboard();
  const label = intl.formatMessage({ id: `task.priority.${task.priority}`.toLowerCase() });
  return (
    <TaskPriorityRoot className={classes.root} task={task}>
      <Chip label={label} />
    </TaskPriorityRoot>
  );
};



const MUI_NAME = 'TaskPriorityRoot';
const TaskPriorityRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})<{ task: TaskApi.Task }>(({ theme, task }) => {
  const bgColor = getPriorityColor(task.priority!);
  const textColor = getContrastText(bgColor);

  return {
    '& .MuiChip-root': {
      backgroundColor: getPriorityColor(task.priority!),
      color: textColor,
    }
  }
})


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
