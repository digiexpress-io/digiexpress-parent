import React from 'react';
import { alpha, generateUtilityClass, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FormattedMessage } from 'react-intl';
import { TaskApi } from '@dxs-ts/task-api';

type Props = { status?: TaskApi.TaskStatus };

export const IndicatorStatus: React.FC<Props> = ({ status }) => {
  const classes = useUtilityClasses();
  if (!status) return null;

  const color = TaskApi.task_status_hex[status] ?? '#ccc5b9';

  return (
    <Root className={classes.root} ownerState={{ color }}>
      <Typography>
        <FormattedMessage {...TaskApi.task_status_messages[status]} />
      </Typography>
    </Root>
  );
};

const MUI_NAME = 'EveliTaskTableStatusIndicator';

const Root = styled('div', {
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
