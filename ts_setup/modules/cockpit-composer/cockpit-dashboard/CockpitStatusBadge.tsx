import React from 'react';
import { alpha, generateUtilityClass, styled, Typography } from '@mui/material';
import { CheckCircle as CheckCircleIcon, Cancel as CancelIcon } from '@mui/icons-material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';

export interface CockpitStatusBadgeProps {
  isActive: boolean;
}

const getStatusColor = (isActive: boolean): string => {
  return isActive ? '#4caf50' : '#f44336'; // Green for active, red for inactive
};

export const CockpitStatusBadge: React.FC<CockpitStatusBadgeProps> = ({ isActive }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  const color = getStatusColor(isActive);
  const label = intl.formatMessage({ id: isActive ? 'cockpit.status.active' : 'cockpit.status.inactive' });

  return (
    <Root className={classes.root} ownerState={{ color }}>
      {isActive ? (<CheckCircleIcon />) : (<CancelIcon />)}
      <Typography>{label}</Typography>
    </Root>
  );
};

const MUI_NAME = 'CockpitStatusBadge';

const Root = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => [styles.root],
})<{ ownerState: { color: string } }>(({ theme, ownerState }) => ({
  backgroundColor: alpha(ownerState.color, 0.1),
  border: `1px solid ${ownerState.color}`,
  paddingLeft: theme.spacing(1.5),
  paddingRight: theme.spacing(1.5),
  paddingTop: theme.spacing(0.5),
  paddingBottom: theme.spacing(0.5),
  borderRadius: theme.spacing(1),
  display: 'inline-flex',
  alignItems: 'center',
  gap: theme.spacing(0.5),
  width: 'fit-content',
  '.MuiTypography-root': {
    fontWeight: 500,
    fontSize: '14px',
    textTransform: 'none',
  },
  '.MuiSvgIcon-root': {
    fontSize: '1.2rem',
    color: ownerState.color,
  },
}));

const useUtilityClasses = () => {
  const slots = { root: ['root'] };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};