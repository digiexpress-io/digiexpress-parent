import React from 'react';
import { Chip, generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';

import { useTaskDashboard } from '../task-dashboard';
import { TaskApi } from '@dxs-ts/task-api';

const getStatusColor = (status: TaskApi.TaskStatus): string => {
  const colorEnum = TaskApi.task_status_colors[status];

  switch (colorEnum) {
    case TaskApi.Colors.RED:
      return '#f44336';
    case TaskApi.Colors.BLUE:
      return '#2196f3';
    case TaskApi.Colors.GREEN:
      return '#4caf50';
    case TaskApi.Colors.YELLOW:
      return '#ffeb3b';
    case TaskApi.Colors.GREY:
      return '#9e9e9e';
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


export const TaskStatusReadOnly: React.FC = () => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const { task } = useTaskDashboard();
  const label = intl.formatMessage({ id: `task.status.${task.status}`.toLowerCase() })


  return (
    <TaskStatusRoot className={classes.root} task={task}>
      <Chip label={label} />
    </TaskStatusRoot>
  );
};



const MUI_NAME = 'TaskStatusReadOnly';
const TaskStatusRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})<{ task: TaskApi.Task }>(({ task }) => {

  const bgColor = getStatusColor(task.status!);
  const textColor = getContrastText(bgColor);

  return {
    '& .MuiChip-root': {
      backgroundColor: getStatusColor(task.status!),
      color: textColor,
    }
  };
})


export const useUtilityClasses = () => {
  const slots = {
    root: ['root']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}