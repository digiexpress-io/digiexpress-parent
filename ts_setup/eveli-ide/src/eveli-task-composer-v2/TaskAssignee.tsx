import React from 'react';
import { Avatar, generateUtilityClass, Stack, styled, Typography } from '@mui/material';
import { TaskApi } from '@/api-task';
import composeClasses from '@mui/utils/composeClasses';


export interface TaskAssigneeProps {
  task: TaskApi.Task;
}

export const TaskAssignee: React.FC<TaskAssigneeProps> = ({ task }) => {
  const classes = useUtilityClasses();

  if (!task.assignedUser || typeof name !== 'string') {
    return (
      <StyledTaskAssignee>
        <Typography>--</Typography>
      </StyledTaskAssignee>)
  }
  const firstName = task.assignedUser.substring(0, task.assignedUser.indexOf(" "));
  const lastName = task.assignedUser.substring(task.assignedUser.indexOf(" ") + 1);
  const firstInitial = firstName.substring(0, 1);
  const secondInitial = lastName.substring(0, 1);

  return (
    <StyledTaskAssignee className={classes.root}>
      <Avatar variant='circular'>
        <Typography>{firstInitial}{secondInitial}</Typography>
      </Avatar>
      <Stack direction='column'>
        <Typography fontWeight={500}>{task.assignedUser}</Typography>
        <Typography variant='caption'>user@gmail.com</Typography>
      </Stack>

    </StyledTaskAssignee>
  )
}




const MUI_NAME = 'TaskAssigneeClassName';
const StyledTaskAssignee = styled('div', {
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
    marginBottom: theme.spacing(1),

    '.MuiAvatar-root': {
      backgroundColor: theme.palette.primary.dark,
      marginRight: theme.spacing(1),
      height: '35px',
      width: '35px',
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