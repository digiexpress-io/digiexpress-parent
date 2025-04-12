import { Popover, styled, generateUtilityClass } from "@mui/material";
import { GPopoverTopicsProps } from "./GPopoverTopics";
import composeClasses from "@mui/utils/composeClasses";
import { margin } from "@mui/system";

export interface GPopoverTopicsClasses {
  root: string;
}
export type GPopoverTopicsClassKey = keyof GPopoverTopicsClasses;

export const MUI_NAME = 'GPopoverTopics';

export const useUtilityClasses = (ownerState: GPopoverTopicsProps) => {
  const slots = {
    root: ['root'],
    popover: ['popover'],
    topics: ['topics'],
    topicsLayout: ['topicsLayout'],
    logoBox: ['logoBox']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GPopoverTopicsRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.topics,
      styles.topicsLayout,
      styles.popover,
      styles.logoBox
    ];
  },
})(({ theme }) => {
  return {   
    [theme.breakpoints.up('lg')]: {
      paddingLeft: theme.spacing(2)
    },
  };
});

export const GTopicsMuiPopover = styled(Popover, {
  name: MUI_NAME,
  slot: 'Popover',
  overridesResolver: (_props, styles) => {
    return [

    ];
  },
})(({ theme }) => {
  return {
    '& .GPopoverTopics-topicsLayout': {
      [theme.breakpoints.up('md')]: {
        width: '32vw'
      },
    },
    '& .MuiPopover-paper': {
      minWidth: '100%',
      left: '0px !important',
      borderRadius: 'unset',
      overflow: 'hidden',

      [theme.breakpoints.down('sm')]: {
        padding: theme.spacing(2),
        width: '100vw',
        minHeight: '100vh',
        overflow: 'auto',
        top: '0px !important',
      },
    },
    '.GPopoverTopics-logoBox': {
      [theme.breakpoints.up('sm')]: {
        display: 'none'
      },
      display: 'flex',
      alignItems: 'center',
      marginBottom: theme.spacing(3),
      justifyContent: 'space-between'
    },
    '& .MuiDivider-root': {
      margin: theme.spacing(1)
    },
    '& .MuiLink-root': {
      display: 'block',
      textDecoration: 'none',
      marginTop: theme.spacing(1),
      marginBottom: theme.spacing(1),
      color: theme.palette.primary.dark,
      fontWeight: theme.typography.fontWeightMedium,
      '&:focus, &:hover, &:visited, &:link, &:active': {
        textDecoration: 'underline'
      }
    }
  };
});

export const GTopics = styled('div', {
  name: MUI_NAME,
  slot: 'Topics',
  overridesResolver: (_props, styles) => {
    return [
      styles.topics,
    ];
  },
})(({ theme }) => {
  return {
    [theme.breakpoints.up('md')]: {
      display: 'flex',
      flexDirection: 'row',
      padding: theme.spacing(1),
      '& .MuiDivider-root': {
        borderRight: `3px solid ${theme.palette.primary.main}`,
      },
      '& .MuiDivider-root:last-of-type': {
        display: 'none',
      },
    },
  };
});