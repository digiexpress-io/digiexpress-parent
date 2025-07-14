import React from 'react';
import { alpha, generateUtilityClass, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { TaskApi } from '@/api-task';

type PriorityType = 'HIGH' | 'NORMAL' | 'LOW';

interface TaskPriority {
  type: PriorityType;
}



function renderPriority(type: PriorityType): { label: string, color: string } {
  switch (type) {
    case 'HIGH':
      return { label: 'High', color: '#d90429' };
    case 'NORMAL':
      return { label: 'Normal', color: '#ffb703' };
    case 'LOW':
      return { label: 'Low', color: '#4cc9f0' };
    default:
      return { label: 'No priority', color: '#ccc5b9' };
  }
};

export const TaskPriority: React.FC<{ task: TaskApi.Task }> = (props) => {
  const classes = useUtilityClasses();
  const { color, label } = renderPriority(props.task.priority as PriorityType);

  return (
    <StyledTaskPriority className={classes.root} ownerState={{ type: props.task.priority as PriorityType, color }}>
      <Typography>{label}</Typography>
    </StyledTaskPriority>
  )
}


const MUI_NAME = 'TaskPriorityClassName'
const StyledTaskPriority = styled('div', {
  name: MUI_NAME,
  slot: 'Priority',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})<{ ownerState: TaskPriority & { color: string } }>(({ theme, ownerState }) => {

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
