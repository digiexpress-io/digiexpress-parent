import React from 'react';
import { generateUtilityClass, styled, TextField, Typography } from '@mui/material';
import { TaskApi } from '@dxs-ts/task-api';
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

  return (
    <StyledTaskAssignee className={classes.root}>
      <StyledTextField value={task.assignedUser} />
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
    marginBottom: theme.spacing(1)
  };
})

const StyledTextField = styled(TextField)(({ theme }) => ({
  width: '100%',
  marginTop: 0,
  '& .MuiOutlinedInput-root': {

  },
  '& .MuiInputBase-input': {
    height: '2.5rem',
    padding: '0 12px'
  },
}));

export const useUtilityClasses = () => {
  const slots = {
    root: ['root']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}