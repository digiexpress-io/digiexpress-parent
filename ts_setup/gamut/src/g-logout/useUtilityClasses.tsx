import { styled, generateUtilityClass } from '@mui/material'
import { GLogoutProps } from './GLogout';
import composeClasses from '@mui/utils/composeClasses';

export const MUI_NAME = 'GLogout';


export interface GLogoutClasses {
  root: string;
  userIdentityLabel: string;
  logoutLayout: string;
  userIdentityText: string;
  userDisplayName: string;
}
export type GLogoutClassKey = keyof GLogoutClasses;

export const useUtilityClasses = (ownerState: GLogoutProps) => {
  const slots = {
    root: ['root'],
    userIdentityLabel: ['userIdentityLabel'],
    logoutLayout: ['logoutLayout'],
    userIdentityText: ['userIdentityText'],
    userDisplayName: ['userDisplayName'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const GLogoutRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => [
    styles.root,
    styles.userIdentityLabel,
    styles.logoutLayout,
  ],
})(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  gap: theme.spacing(2),

  '& .GLogout-userIdentityLabel': {
    textAlign: 'right',
    color: theme.palette.text.secondary,
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'center',

    '& .GLogout-userIdentityText': {
      color: theme.palette.text.secondary,
      fontSize: 'small',
    },
    
    '& .GLogout-userDisplayName': {
      fontWeight: theme.typography.fontWeightBold,
      color: theme.palette.text.primary,
      fontSize: 'large',
    },
  },

  '& .GLogout-logoutLayout': {
    display: 'flex',
    alignItems: 'center',
    gap: theme.spacing(2),
  },
}));
