import React from 'react';
import { alpha, generateUtilityClass, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';

export interface CockpitStatusIndicatorProps {
  isActive: boolean;
}

const getStatusColor = (isActive: boolean): string => {
  return isActive ? '#4caf50' : '#f44336'; // Green for active, red for inactive
};

export const CockpitStatusIndicator: React.FC<CockpitStatusIndicatorProps> = ({ isActive }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  const color = getStatusColor(isActive);
  const label = intl.formatMessage({id: isActive ? 'cockpit.status.active' : 'cockpit.status.inactive'});

  return (
    <Root className={classes.root} ownerState={{ color }}>
      <Typography>{label}</Typography>
    </Root>
  );
};

const MUI_NAME = 'CockpitStatusIndicator';

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