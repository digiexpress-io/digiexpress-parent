import { Popover, styled, generateUtilityClass } from "@mui/material";
import { GPopoverTopicsProps } from "./GPopoverTopics";
import composeClasses from "@mui/utils/composeClasses";

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
    topicsLayout: ['topicsLayout']
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
      styles.popover
    ];
  },
})(({ theme }) => {
  return {

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
    [theme.breakpoints.down('sm')]: {
      maxHeight: '400px'
    },
    '& .GPopoverTopics-topicsLayout': {
      [theme.breakpoints.up('md')]: {
        width: '32vw'
      },
    },
    '& .MuiPopover-paper': {
      minWidth: '100%',
      left: '0px !important',
      borderRadius: 'unset',
      padding: theme.spacing(1),
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
        borderRight: `1px solid ${theme.palette.text.disabled}`,
      },
    },

    [theme.breakpoints.down('md')]: {
      display: 'flex',
      flexDirection: 'column',
      '& .MuiDivider-root': {
        display: 'none'
      }
    }
  };
});