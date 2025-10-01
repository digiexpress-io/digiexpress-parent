import React from 'react';
import { alpha, generateUtilityClass, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FormattedMessage } from 'react-intl';

import { TaskApi } from '@dxs-ts/task-api';


function renderStatus(type: TaskApi.TaskStatus): { label: string, color: string } {
  switch (type) {
    case TaskApi.TaskStatus.NEW:
      return { label: 'task.status.new', color: '#ffb703' };
    case TaskApi.TaskStatus.OPEN:
      return { label: 'task.status.open', color: '#70e000' };
    case TaskApi.TaskStatus.COMPLETED:
      return { label: 'task.status.completed', color: '#48bfe3' };
    case TaskApi.TaskStatus.REJECTED:
      return { label: 'task.status.rejected', color: '#d90429' };
    case TaskApi.TaskStatus.TRANSFERRED:
      return { label: 'task.status.transferred', color: '#d90429' };
    case TaskApi.TaskStatus.DELEGATED:
      return { label: 'task.status.delegated', color: '#d90429' };
    case TaskApi.TaskStatus.WAITING:
      return { label: 'task.status.waiting', color: '#ffff00' };
    default:
      return { label: 'eveli.noValueIndicator', color: '#ccc5b9' };
  }
};

export const IndicatorStatus: React.FC<{
  status: TaskApi.TaskStatus
}> = (props) => {
  const classes = useUtilityClasses();
  const values = renderStatus(props.status);

  return (
    <IndicatorStatusRoot className={classes.root} ownerState={{ ...props, color: values.color }}>
      <Typography><FormattedMessage id={values.label} /></Typography>
    </IndicatorStatusRoot>
  )
}


const MUI_NAME = 'EveliTaskTableStatusIndicator'
const IndicatorStatusRoot = styled('div', {
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
