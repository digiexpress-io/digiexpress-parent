import React from 'react';
import { Chip, generateUtilityClass, styled, Typography } from '@mui/material';
import { TaskApi } from '@/api-task';
import composeClasses from '@mui/utils/composeClasses';


export interface TaskRolesProps {
  task: TaskApi.Task;
}

export const TaskRoles: React.FC<TaskRolesProps> = ({ task }) => {
  const classes = useUtilityClasses();

  if (!task.assignedRoles || task.assignedRoles.length === 0) {
    return (
      <TaskRolesRoot>
        <Typography>--</Typography>
      </TaskRolesRoot>)
  }

  return (
    <TaskRolesRoot className={classes.root}>
      {task.assignedRoles.map(role => <Chip label={role} variant='filled' />)}
    </TaskRolesRoot>
  )
}




const MUI_NAME = 'TaskRolesRootClassName';
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
      boxShadow: '0 4px 12px rgba(0, 0, 0, 0.2)'
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
