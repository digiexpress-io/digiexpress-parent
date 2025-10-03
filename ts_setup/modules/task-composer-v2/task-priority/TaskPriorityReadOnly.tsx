import React from 'react';
import { Chip, generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';

import { TaskApi } from '@dxs-ts/task-api';
import { useTaskDashboard } from '../task-dashboard';
import { PriorityHex, getContrastText } from '../../eveli-primitives/eveli-theme';

type Priority = TaskApi.TaskPriority;

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
  const bgColor = PriorityHex[task.priority!];
  const textColor = getContrastText(bgColor);

  return {
    '& .MuiChip-root': {
      backgroundColor: PriorityHex[task.priority!],
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
