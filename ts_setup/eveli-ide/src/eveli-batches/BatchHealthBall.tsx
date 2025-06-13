import React from 'react';
import { styled, Tooltip } from '@mui/material';
import CircleIcon from '@mui/icons-material/Circle';
import { generateUtilityClass } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';


/*
   Batch health colours

 * Grey: Batch created but not yet run
 * Yellow: Batch completed with some failures
 * Yellow-flashing: Batch running with some failures
 * Red: Batch critical error, run failed
 * Green: Batch completed successfully
 * Green-flashing: Batch currently running
 */


type BatchHealthType = 'CREATED_NOT_RUN' | 'RUNNING_NO_FAILS' | 'RUNNING_SOME_FAILS' | 'COMPLETED_SOME_FAILS' | 'COMPLETED_SUCCESS' | 'FAILED_CRITICAL';

interface BatchHealthBallProps {
  health: BatchHealthType;
}

export const BatchHealthBall: React.FC<BatchHealthBallProps> = ({ health }) => {
  const intl = useIntl();
  const classes = useUtilityClasses(health);
  const tooltip = getHealthConfig(health);

  return (
    <BatchHealthBallRoot className={classes.root}>
      <Tooltip title={intl.formatMessage({ id: tooltip.intl })}>
        <CircleIcon className={classes.healthColor} />
      </Tooltip>
    </BatchHealthBallRoot>
  )
}


export const MUI_NAME = 'BatchHealthBall';

export interface BatchHealthBallClasses {
  root: string;
  healthColorCreated: string;
  healthColorFlashingSuccess: string;
  healthColorFlashingWarning: string;
  healthColorWarning: string;
  healthColorSuccess: string;
  healthColorCriticalFail: string;
  healthColorDefault: string;
}

export type BatchHealthBallClassKey = keyof BatchHealthBallClasses;

export const useUtilityClasses = (health: BatchHealthType) => {
  const { classKey } = getHealthConfig(health);

  const slots = {
    root: ['root'],
    healthColor: [classKey],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const BatchHealthBallRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
})(({ theme }) => ({

  '& .BatchHealthBall-healthColorCreated': {
    color: theme.palette.action.disabled,
  },
  '& .BatchHealthBall-healthColorSuccess': {
    color: theme.palette.success.main,
  },
  '& .BatchHealthBall-healthColorWarning': {
    color: theme.palette.warning.main,
  },
  '& .BatchHealthBall-healthColorCriticalFail': {
    color: theme.palette.error.main,
  },
  '& .BatchHealthBall-healthColorDefault': {
    color: theme.palette.text.primary,
  },
  '& .BatchHealthBall-healthColorFlashingSuccess': {
    animation: 'BatchHealthBall-healthColorFlashingSuccess 1s infinite',
    fill: theme.palette.success.light,
  },
  '& .BatchHealthBall-healthColorFlashingWarning': {
    animation: 'BatchHealthBall-healthColorFlashingWarning 1s infinite',
    fill: theme.palette.warning.light,
  },
  '@keyframes BatchHealthBall-healthColorFlashingSuccess': {
    '0%': { fill: theme.palette.success.light },
    '50%': { fill: theme.palette.success.main },
    '100%': { fill: theme.palette.success.light },
  },
  '@keyframes BatchHealthBall-healthColorFlashingWarning': {
    '0%': { fill: theme.palette.warning.light },
    '50%': { fill: theme.palette.warning.main },
    '100%': { fill: theme.palette.warning.light },
  }

}));

function getHealthConfig(health: BatchHealthType): { classKey: BatchHealthBallClassKey, intl: string } {
  switch (health) {
    case 'CREATED_NOT_RUN':
      return { classKey: 'healthColorCreated', intl: 'Batch created but not yet run' };
    case 'RUNNING_NO_FAILS':
      return { classKey: 'healthColorFlashingSuccess', intl: 'Batch running successfully' };
    case 'RUNNING_SOME_FAILS':
      return { classKey: 'healthColorFlashingWarning', intl: 'Batch running with some failures' };
    case 'COMPLETED_SOME_FAILS':
      return { classKey: 'healthColorWarning', intl: 'Batch completed with some failures' };
    case 'COMPLETED_SUCCESS':
      return { classKey: 'healthColorSuccess', intl: 'Batch completed successfully' };
    case 'FAILED_CRITICAL':
      return { classKey: 'healthColorCriticalFail', intl: 'Batch failed with critical errors' };
    default:
      return { classKey: 'healthColorDefault', intl: 'Batch health unknown' };
  }

};




