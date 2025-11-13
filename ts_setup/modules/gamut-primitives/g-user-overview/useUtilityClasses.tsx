import { generateUtilityClass, styled, Box, alpha } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { GUserOverviewProps } from './GUserOverview';
import { GUserOverviewDetailProps } from './GUserOverviewDetail';


export const MUI_NAME = 'GUserOverview';

export interface GUserOverviewClasses {
  root: string;
}

export type GUserOverviewClassKey = keyof GUserOverviewClasses;

export interface GUserOverviewDetailClasses {
  root: string
}
export type GUserOverviewDetailClassKey = keyof GUserOverviewDetailClasses;


export const GUserOverviewDetail = styled(Box, {
  name: MUI_NAME,
  slot: 'OverviewItem',
  overridesResolver: (_props, styles) => {
    return [
      styles.overviewItem
    ];
  },
})<{ ownerState: GUserOverviewDetailProps }>(({ theme, ownerState }) => {

  return {
    display: 'flex',
    flexDirection: 'column',
    cursor: ownerState.onClick ? 'pointer' : 'auto',
    backgroundColor: theme.palette.background.default,
    border: `1px solid ${theme.palette.divider}`,

    ':hover': ownerState.onClick ? {
      backgroundColor: theme.palette.action.active,
      borderColor: 'rgba(194,190,194,1)',
    } : undefined,

    '& .GUserOverview-overviewItemTitle': {
      ...theme.typography.h4,
      padding: theme.spacing(2),
      textAlign: 'left',
    },  
    '& .GUserOverview-overviewItemCount': {
      display: 'flex',
      alignItems: 'center',
      padding: theme.spacing(2),
    },
    '& .GUserOverview-overviewItemCountAvatar': {
      backgroundColor: alpha(theme.palette.primary.light, 0.1),
      color: theme.palette.text.primary,
      marginRight: theme.spacing(1),
      height: '60px',
      width: '60px'
    },
    '& .GUserOverview-overviewItemCountAvatarLabel': {
      ...theme.typography.h1
    },
    '& .GUserOverview-overviewItemButtonLabel': {
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
    ];
  },
})<{ ownerState: GUserOverviewProps }>(({ theme }) => {

  return {
    display: 'flex',
    justifyContent: 'center',
    marginTop: theme.spacing(3),
    marginBottom: theme.spacing(1),
    '& .GUserOverviewItem-serviceSelect': {
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

    overviewItem: ['overviewItem'],
    overviewItemTitle: ['overviewItemTitle'],
    overviewItemCount: ['overviewItemCount'],
    overviewItemCountAvatar: ['overviewItemCountAvatar'],
    overviewItemCountAvatarLabel: ['overviewItemCountAvatarLabel'],
    overviewItemButtonLabel: ['overviewItemButtonLabel']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
