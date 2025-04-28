import React from 'react';
import { alpha, generateUtilityClass, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

type StatusType = 'NEW' | 'OPEN' | 'COMPLETED' | 'REJECTED';

interface TaskStatus {
  type: StatusType;
}

function renderStatus(type: StatusType): { label: string, color: string } {
  switch (type) {
    case 'NEW':
      return { label: 'New', color: '#ffb703' };
    case 'OPEN':
      return { label: 'Open', color: '#70e000' };
    case 'COMPLETED':
      return { label: 'Completed', color: '#48bfe3' };
    case 'REJECTED':
      return { label: 'Rejected', color: '#d90429' };
    default:
      return { label: 'No status', color: '#ccc5b9' };
  }
};

export const IndicatorStatus: React.FC<TaskStatus> = (props) => {
  const classes = useUtilityClasses();
  const values = renderStatus(props.type);

  return (
    <IndicatorStatusRoot className={classes.root} ownerState={{ ...props, color: values.color }}>
      <Typography>{values.label}</Typography>
    </IndicatorStatusRoot>
  )
}


const MUI_NAME = 'IndicatorStatusRootClassName'
const IndicatorStatusRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Status',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})<{ ownerState: TaskStatus & { color: string } }>(({ theme, ownerState }) => {

  return {
    backgroundColor: alpha(ownerState.color, 0.2),
    border: `1px solid ${ownerState.color}`,
    paddingLeft: theme.spacing(0.5),
    paddingRight: theme.spacing(0.5),
    borderRadius: theme.spacing(0.5),
    display: 'inline-flex',
    alignItems: 'center',
    width: 'fit-content',
    '.MuiTypography-root': {
      fontWeight: 500,
      fontSize: '9pt',
      textTransform: 'uppercase'
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
