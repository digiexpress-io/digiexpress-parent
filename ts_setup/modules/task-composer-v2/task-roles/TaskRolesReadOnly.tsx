import React from 'react';
import { Chip, generateUtilityClass, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';
import { TaskApi } from '@dxs-ts/task-api';
import { TaskCardStyleDefinition } from '../task-card';

export interface TaskRolesReadOnlyProps {
  task: TaskApi.Task;
  style: TaskCardStyleDefinition
}

export const TaskRolesReadOnly: React.FC<TaskRolesReadOnlyProps> = ({ task, style }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();

  if (!task.assignedRoles || task.assignedRoles.length === 0) {
    return (
      <TaskRolesRoot>
        <Typography color='error' sx={{ ...style.bodyTypography }}>{intl.formatMessage({ id: 'task.roles.none' })}</Typography>
      </TaskRolesRoot>)
  }

  return (
    <TaskRolesRoot className={classes.root}>
      {task.assignedRoles.map((role, index) => <Chip key={index} label={role} variant='filled' />)}
    </TaskRolesRoot>
  )
}




const MUI_NAME = 'TaskRolesReadOnly';
const TaskRolesRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})(({ theme }) => {

  return {
    display: 'flex',
    alignItems: 'center',
    flexWrap: 'wrap',
    marginBottom: theme.spacing(1),
    gap: theme.spacing(1),

    '& .MuiChip-root': {
      backgroundColor: theme.palette.info.dark,
      color: theme.palette.background.default,
      // boxShadow: '0 4px 12px rgba(0, 0, 0, 0.2)'
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
