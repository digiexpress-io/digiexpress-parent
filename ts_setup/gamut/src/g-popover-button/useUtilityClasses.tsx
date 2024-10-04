import { generateUtilityClass, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { GPopoverButtonProps } from './GPopoverButton';


export const MUI_NAME = 'GPopoverButton';

export interface GPopoverButtonClasses {
  root: string;
  button: string;
  iconButton: string
}

export type GPopoverButtonClassKey = keyof GPopoverButtonClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    button: ['button'],
    iconButton: ['iconButton']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const PopoverButton = styled(Typography, {
  name: MUI_NAME,
  slot: 'Button',
  overridesResolver: (_props, styles) => {
    return [
      styles.button,
      styles.iconButton
    ];
  },
})<{ ownerState: GPopoverButtonProps }>(({ theme, ownerState }) => {
  return {
    fontSize: theme.typography.h3.fontSize,
    display: 'inline-flex',
    height: 'max-content',
    alignItems: "center",
    padding: theme.spacing(2),
    position: "relative",
    justifyContent: 'space-between',
    zIndex: 1,

    '&::before': {
      content: '""',
      position: 'absolute',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      background: 'rgba(255, 255, 255, 0.7)',
      backdropFilter: 'blur(3px)',
      zIndex: -1,
      borderRadius: 'inherit',
    },

    '& .GPopoverButton-iconButton ': {
      transform: ownerState.iconRotated ? 'rotate(180deg)' : 'rotate(0deg)',
      transition: "transform 0.3s ease-in-out",
      marginLeft: theme.spacing(1),
      backgroundColor: theme.palette.primary.main,
      color: theme.palette.primary.contrastText,
      '&:hover': {
        backgroundColor: theme.palette.primary.main
      },
    },

    '& .GPopoverButton-iconButton .MuiSvgIcon-root ': {
      [theme.breakpoints.up('md')]: {
        fontSize: 'xx-large'
      },
      [theme.breakpoints.down('md')]: {
        fontSize: 'large'
      }
    },

    '&:hover': {
      textDecoration: 'underline',
      cursor: 'pointer',
    },
    [theme.breakpoints.up('md')]: {
      marginRight: theme.spacing(2),
      minWidth: '20vw',
    },
    [theme.breakpoints.down('md')]: {
      width: '90vw',
    }
  };
});

export const GPopoverButtonRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },
})(({ theme, }) => {
  return {


  };
});