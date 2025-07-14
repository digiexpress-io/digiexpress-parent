import React from 'react';
import { alpha, generateUtilityClass, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { TaskApi } from '@/api-task';


function renderStatus(task: TaskApi.Task): { label: string, color: string } {
  switch (task.status) {
    case TaskApi.TaskStatus.NEW:
      return { label: 'New', color: '#ffb703' };
    case TaskApi.TaskStatus.OPEN:
      return { label: 'Open', color: '#70e000' };
    case TaskApi.TaskStatus.COMPLETED:
      return { label: 'Completed', color: '#48bfe3' };
    case TaskApi.TaskStatus.REJECTED:
      return { label: 'Rejected', color: '#d90429' };
    case TaskApi.TaskStatus.TRANSFERRED:
      return { label: 'Transferred', color: '#d90429' };
    case TaskApi.TaskStatus.DELEGATED:
      return { label: 'Delegated', color: '#d90429' };
    case TaskApi.TaskStatus.WAITING:
      return { label: 'Waiting', color: '#ffff00' };
    default:
      return { label: 'No status', color: '#ccc5b9' };
  }
};

export const TaskStatus: React.FC<{
  task: TaskApi.Task
}> = (props) => {
  const classes = useUtilityClasses();
  const values = renderStatus(props.task);

  return (
    <StyledTaskStatus className={classes.root} ownerState={{ ...props, color: values.color }}>
      <Typography>{values.label}</Typography>
    </StyledTaskStatus>
  )
}


const MUI_NAME = 'EveliTaskStatus'
const StyledTaskStatus = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})<{ ownerState: { color: string } }>(({ theme, ownerState }) => {

  return {
    backgroundColor: alpha(ownerState.color, 0.2),
    border: `1px solid ${ownerState.color}`,
    paddingLeft: theme.spacing(0.5),
    paddingRight: theme.spacing(0.5),
    borderRadius: theme.spacing(0.5),
    alignItems: 'center',
    minWidth: '40%',
    maxWidth: '50%',
    '.MuiTypography-root': {
      fontWeight: 500,
      textTransform: 'uppercase',
      textAlign: 'center'
    }
  };
});

const useUtilityClasses = () => {
  const slots = {
    root: ['root'],

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
