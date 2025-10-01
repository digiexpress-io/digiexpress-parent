import React from 'react';
import { alpha, generateUtilityClass, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FormattedMessage } from 'react-intl';

type PriorityType = 'HIGH' | 'NORMAL' | 'LOW';

interface TaskPriority {
  type: PriorityType;
}

function renderPriority(type: PriorityType): { label: string, color: string } {
  switch (type) {
    case 'HIGH':
      return { label: 'task.priority.high', color: '#d90429' };
    case 'NORMAL':
      return { label: 'task.priority.normal', color: '#ffb703' };
    case 'LOW':
      return { label: 'task.priority.low', color: '#4cc9f0' };
    default:
      return { label: 'eveli.noValueIndicator', color: '#ccc5b9' };
  }
};

export const IndicatorPriority: React.FC<TaskPriority> = (props) => {
  const classes = useUtilityClasses();
  const values = renderPriority(props.type);

  return (
    <IndicatorPriorityRoot className={classes.root} ownerState={{ ...props, color: values.color }}>
      <Typography><FormattedMessage id={values.label} /></Typography>
    </IndicatorPriorityRoot>
  )
}


const MUI_NAME = 'IndicatorPriorityRootClassName'
const IndicatorPriorityRoot = styled('div', {
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
