import { Popover, styled, generateUtilityClass, alpha } from "@mui/material";
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
    topicsLayout: ['topicsLayout'],
    logoBox: ['logoBox'],
    childTopic: ['childTopic']
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
      styles.logoBox,
      styles.childTopic
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
      [theme.breakpoints.up('sm')]: {
        paddingLeft: theme.spacing(3),
      },
      [theme.breakpoints.up('lg')]: {
        paddingLeft: theme.spacing(2),
        width: '32vw'
      },
    },
    '& .GPopoverTopics-topicsLayout .GPopoverTopics-childTopic:not(:first-of-type)': {
      marginBottom: '0px'
    },

    '& .GPopoverTopics-topicsLayout > .GPopoverTopics-childTopic': {
      display: 'flex',
      alignItems: 'center',
      ...theme.typography.body2,
      color: alpha(theme.palette.text.primary, 0.8),
      marginLeft: theme.spacing(1),
      marginTop: '0px',
      marginBottom: '0px',
      [theme.breakpoints.down('sm')]: {
        marginTop: theme.spacing(0.5),
        marginBottom: theme.spacing(0.5)
      }
    },
    '& .GPopoverTopics-topicsLayout > .GPopoverTopics-childTopic .MuiSvgIcon-root': {
      fontSize: '6pt',
      marginRight: theme.spacing(1),
      color: theme.palette.primary.main
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
    '& .MuiDivider-vertical': {
      margin: theme.spacing(1)
    },
    '& .MuiLink-root': {
      display: 'block',
      textDecoration: 'none',
      marginTop: theme.spacing(1),
      color: theme.palette.text.primary,
      ...theme.typography.body1,
      fontWeight: 'bold',
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
        marginLeft: theme.spacing(2),
        marginRight: theme.spacing(2)
      },      
      '& .MuiDivider-vertical:last-of-type': {
        display: 'none'
      }
    },
  };
});