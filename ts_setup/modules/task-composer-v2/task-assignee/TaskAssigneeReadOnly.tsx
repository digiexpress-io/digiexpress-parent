import React from 'react';
import { generateUtilityClass, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';
import { TaskApi } from '@dxs-ts/task-api';
import { TaskCardStyleDefinition } from '../task-card';


export interface TaskAssigneeReadOnlyProps {
  task: TaskApi.Task;
  style: TaskCardStyleDefinition
}

export const TaskAssigneeReadOnly: React.FC<TaskAssigneeReadOnlyProps> = ({ task, style }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();

  if (!task.assignedUser || typeof name !== 'string') {
    return (
      <StyledTaskAssigneeReadOnly>
        <Typography color='error' sx={{ ...style.bodyTypography }}>{intl.formatMessage({ id: 'task.assignees.none' })}</Typography>
      </StyledTaskAssigneeReadOnly>
    )
  }

  return (
    <StyledTaskAssigneeReadOnly className={classes.root}>
      <Typography>{task.assignedUser}</Typography>
    </StyledTaskAssigneeReadOnly>
  )
}




const MUI_NAME = 'TaskAssigneeReadOnly';
const StyledTaskAssigneeReadOnly = styled('div', {
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
    marginBottom: theme.spacing(1)
  };
})


export const useUtilityClasses = () => {
  const slots = {
    root: ['root']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}