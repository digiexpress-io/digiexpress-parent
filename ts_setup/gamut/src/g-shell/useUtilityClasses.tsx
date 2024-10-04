import { styled, alpha, generateUtilityClass } from '@mui/material'
import composeClasses from '@mui/utils/composeClasses'

export const GShellClassName = 'GShellBase';

export const MUI_NAME = 'GShell';
export const useUtilityClasses = () => {
  const slots = {
    root: ['root']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}



export const GShellRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },
})<{
  ownerState: {
    toolbarHeight: number;
    footerHeight: number;
    drawerWidth: number;
    drawerOpen: boolean;
  }
}>(({ theme, ownerState }) => {

  const {
    toolbarHeight,
    footerHeight,
    drawerWidth,
    drawerOpen,
  } = ownerState;


  return {
    display: 'flex',
    flexDirection: 'column',

    "& main": {
      flex: 1,
      width: "100%",
      minHeight: '100vh',
      backgroundColor: theme.palette.background.paper,
    },

    "& .GAppBar-root": {
      position: 'sticky',
      top: '0',
      zIndex: theme.zIndex.drawer + 1,
    },

    "& .MuiToolbar-root.GShellBase": {
      display: 'flex',
      flexDirection: 'column',
      paddingLeft: 'unset',
      paddingRight: 'unset',
      position: 'absolute',
      zIndex: theme.zIndex.drawer + 1,
      width: '100%',
      backgroundColor: theme.palette.background.paper,
      boxShadow: `0 0 7px ${theme.palette.text.disabled}`,
      height: toolbarHeight + 'px'

    },

    '& .MuiToolbar-root.GShellBase .MuiDivider-root': {
      borderBottom: `1px solid ${alpha(theme.palette.text.disabled, 0.1)}`,
    },
    "& .MuiContainer-root": {
      maxWidth: 'none',
      overflow: 'auto',
      padding: 'unset',

      marginTop: `${toolbarHeight}px`,
      minHeight: `calc(100% - (${toolbarHeight}px))`,

      [theme.breakpoints.up('md')]: {
        marginLeft: drawerOpen ? `${drawerWidth}px` : undefined, //drawer width expanded
        width: `calc(100% - ${drawerOpen ? drawerWidth : undefined}px)`,
      },
      [theme.breakpoints.down('md')]: {
        width: `100%`,
      }
    },
    '& .GFooter-root': {
      height: `${footerHeight}px`,
      [theme.breakpoints.up('md')]: {
        marginLeft: drawerOpen ? `${drawerWidth}px` : undefined //drawer width expanded
      }
    },

    '& .GShellBase .MuiDrawer-paper': {
      [theme.breakpoints.down('md')]: {
        display: 'none'
      },
      [theme.breakpoints.up('md')]: {
        paddingTop: toolbarHeight + 10,
        paddingLeft: theme.spacing(1),
        paddingRight: theme.spacing(1),
        borderRight: `1px solid ${theme.palette.divider}`,
        width: drawerWidth,
        transition: theme.transitions.create('width', {
          easing: theme.transitions.easing.sharp,
          duration: theme.transitions.duration.enteringScreen,
        }),
        boxSizing: 'border-box',
        ...(!drawerOpen && {
          overflowX: 'hidden',
          transition: theme.transitions.create('width', {
            easing: theme.transitions.easing.sharp,
            duration: theme.transitions.duration.leavingScreen,
          })
        }),
      }
    },
  };
});