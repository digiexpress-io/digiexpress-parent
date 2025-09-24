import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';


export const MUI_NAME = 'GAppBar';

export interface GAppBarClasses {
  root: string;
  buttonLayout: string;
  rightSideLayout: string;
  userIdentityLabel: string;
  userIdentityText: string;
  userDisplayName: string;
}

export type GAppBarClassKey = keyof GAppBarClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    buttonLayout: ['buttonLayout'],
    rightSideLayout: ['rightSideLayout'],
    userIdentityLabel: ['userIdentityLabel'],
    userIdentityText: ['userIdentityText'],
    userDisplayName: ['userDisplayName'],
  };

  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const GAppBarRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => [
    styles.root,
    styles.buttonLayout,
    styles.rightSideLayout,
    styles.userIdentityLabel,
    styles.userIdentityText,
    styles.userDisplayName,
  ],
})<{  }>(({ theme }) => ({
  display: 'flex',

  '& .GAppBar-rightSideLayout': {
    display: 'flex',
    alignItems: 'center',
    gap: theme.spacing(2),

    [theme.breakpoints.down('sm')]: {
      flexDirection: 'column',
      alignItems: 'center',
      gap: 0,
    },
  },

  '& .GAppBar-buttonLayout': {
    display: 'flex',
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing(1),

    [theme.breakpoints.down('sm')]: {
      order: 1,
      flexWrap: 'wrap',
    },
  },

  '& .GAppBar-userIdentityLabel': {
    color: theme.palette.text.secondary,
    display: 'flex',
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'baseline',
    gap: theme.spacing(0.5),

    [theme.breakpoints.down('sm')]: {
      justifyContent: 'center',
      order: 2,
      width: '100%',
    },

    '& .GAppBar-userIdentityText': {
      color: theme.palette.text.secondary,
      fontSize: 'small',
      maxWidth: '100%',
      whiteSpace: 'nowrap',

      [theme.breakpoints.down('sm')]: {
        display: 'none',
      },
    },

    '& .GAppBar-userDisplayName': {
      fontWeight: theme.typography.fontWeightBold,
      color: theme.palette.text.primary,
      fontSize: 'large',

      [theme.breakpoints.down('sm')]: {
        fontSize: 'medium',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap',
        maxWidth: '160px',
        display: 'inline-block',
      },
    },
  },
}));


