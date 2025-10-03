import React from 'react';
import { alpha, generateUtilityClass, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FormattedMessage } from 'react-intl';
import { TaskApi } from '@dxs-ts/task-api';
import { PriorityHex } from '../../eveli-primitives/eveli-theme';

type PriorityType = 'HIGH' | 'NORMAL' | 'LOW';

const mapToApiKey: Record<PriorityType, TaskApi.TaskPriority> = {
  HIGH: TaskApi.TaskPriority.HIGH,
  NORMAL: TaskApi.TaskPriority.NORMAL,
  LOW: TaskApi.TaskPriority.LOW,
};

export const IndicatorPriority: React.FC<{ type: PriorityType }> = ({ type }) => {
  const classes = useUtilityClasses();

  const apiPriority = mapToApiKey[type];
  const color = PriorityHex[apiPriority];
  const labelId = `task.priority.${type.toLowerCase()}`;

  return (
    <IndicatorPriorityRoot className={classes.root} ownerState={{ color }}>
      <Typography><FormattedMessage id={labelId} /></Typography>
    </IndicatorPriorityRoot>
  );
};

const MUI_NAME = 'EveliTaskTablePriorityIndicator';

const IndicatorPriorityRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => [styles.root],
})<{ ownerState: { color: string } }>(({ theme, ownerState }) => ({
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
    textTransform: 'uppercase',
  },
}));

const useUtilityClasses = () => {
  const slots = { root: ['root'] };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};