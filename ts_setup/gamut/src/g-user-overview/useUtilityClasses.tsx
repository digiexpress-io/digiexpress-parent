import { generateUtilityClass, styled, Box, lighten, alpha } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { GUserOverviewProps } from './GUserOverview';
import { GUserOverviewDetailProps } from './GUserOverviewDetail';


export const MUI_NAME = 'GUserOverview';

export interface GUserOverviewClasses {
  root: string;
  serviceSelect: string;
}

export type GUserOverviewClassKey = keyof GUserOverviewClasses;

export interface GUserOverviewDetailClasses {
  root: string;
  count: string;
  title: string;
  countAvatar: string;
  countAvatarLabel: string;
  buttonLabel: string;
}
export type GUserOverviewDetailClassKey = keyof GUserOverviewDetailClasses;


export const GUserOverviewDetailRoot = styled(Box, {
  name: MUI_NAME,
  slot: 'Item',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.count,
      styles.title,
      styles.countAvatar,
      styles.countAvatarLabel,
      styles.buttonLabel
    ];
  },
})<{ ownerState: GUserOverviewDetailProps }>(({ theme, ownerState }) => {

  return {
    display: 'flex',
    flexDirection: 'column',
    cursor: ownerState.onClick ? 'pointer' : 'auto',
    backgroundColor: `${lighten(theme.palette.action.disabled, 0.85)}`,
    borderWidth: '1px',
    borderStyle: 'solid',
    borderColor: lighten(theme.palette.action.disabled, 0.5),
    ':hover': ownerState.onClick ? {
      backgroundColor: `${lighten(theme.palette.action.disabled, 0.7)}`,
      borderColor: 'rgba(194,190,194,1)',
      boxShadow: '0px 7px 5px -3px rgba(194,190,194,0.7)',
    } : undefined,
    '& .GUserOverview-title': {
      ...theme.typography.h4,
      padding: theme.spacing(2),
      textAlign: 'left',
    },
    '& .GUserOverview-count': {
      display: 'flex',
      alignItems: 'center',
      padding: theme.spacing(2),
    },
    '& .GUserOverview-countAvatar': {
      backgroundColor: alpha(theme.palette.primary.light, 0.1),
      color: theme.palette.text.primary,
      marginRight: theme.spacing(1),
      height: '60px',
      width: '60px'
    },
    '& .GUserOverview-countAvatarLabel': {
      ...theme.typography.h1
    },
    '& .GUserOverview-buttonLabel': {
      ...theme.typography.body1
    },

    [theme.breakpoints.up('md')]: {
      minHeight: '20vh',
    },
    [theme.breakpoints.down('md')]: {
      minHeight: '10vh',
    },
  };
});

export const GUserOverviewRoot = styled(Box, {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      styles.serviceSelect
    ];
  },
})<{ ownerState: GUserOverviewProps }>(({ theme }) => {

  return {
    display: 'flex',
    justifyContent: 'center',
    marginTop: theme.spacing(3),
    marginBottom: theme.spacing(1),
    '& .GUserOverview-serviceSelect': {
      [theme.breakpoints.up('md')]: {
        display: 'none'
      }
    },
    "span": {
      display: 'flex',
      alignItems: 'center'
    },
    "& .MuiSvgIcon-root": {
      marginRight: theme.spacing(1),
      fontSize: '20px'
    },
  };
});

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    serviceSelect: ['serviceSelect'],
    count: ['count'],
    title: ['title'],
    countAvatar: ['countAvatar'],
    countAvatarLabel: ['countAvatarLabel'],
    buttonLabel: ['buttonLabel']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
