import { styled, generateUtilityClass } from '@mui/material'
import composeClasses from '@mui/utils/composeClasses'

export const EveliShellClassName = 'EveliShellBase';
export const EveliShellMiniBarClassName = 'EveliShellMiniBar';
export const EveliShellLargeBarClassName = 'EveliShellLargeBar';

export const MUI_NAME = 'EveliShell';
export const useUtilityClasses = () => {
  const slots = {
    root: ['root']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

export const EveliShellRoot = styled('div', {
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

    expanded?: number;
    collapsed?: number;
  }
}>(({ theme, ownerState }) => {

  const minibarWidth = 60;
  const drawerWidth = ownerState.drawerOpen ? ownerState.drawerWidth :  minibarWidth;
  const largebarWidth = ownerState.drawerOpen ? drawerWidth - minibarWidth: 0;

  const {
    toolbarHeight,
    footerHeight,  
    drawerOpen,
  } = ownerState;

  return {
    display: 'flex',
    flexDirection: 'column',

    
    '& .EveliLocales-root': {
      width: `${drawerWidth}px`,
      display: drawerOpen ? undefined : 'none'
    },
    
    '& .EveliShellMiniBar': {
      display: 'flex',
      flexDirection: 'column',
      width: `${minibarWidth -1}px`,
      borderRight: drawerOpen ? `1px solid ${theme.palette.secondary.contrastText}` : undefined,
    },
    "& .EveliShellMiniBar .MuiButtonBase-root": {
      minWidth: "unset",
      color: theme.palette.primary.contrastText,
    },
    "& .EveliShellMiniBar .Mui-selected": {
      color: theme.palette.secondary.contrastText,
    },
    "& .EveliShellMiniBar .MuiTabs-indicator": {
      backgroundColor: theme.palette.secondary.contrastText,
      marginRight: "49px"
    },

    "& .EveliAppBar-root": {
      position: 'sticky',
      top: '0',
      zIndex: theme.zIndex.drawer + 1,
    },

    '& .EveliShellLargeBar': {
      width: `${largebarWidth -1}px`,
      display: drawerOpen ? undefined : 'none' 
    },

    '& .EveliShellBase .MuiDrawer-paper': {    
      backgroundColor: theme.palette.secondary.main,
      boxSizing: 'border-box',     
      paddingTop: toolbarHeight,
      width: drawerWidth,

      display: 'flex',
      flexGrow: 1,
      flexDirection: 'row',

      borderRight: `1px solid ${theme.palette.divider}`,
      
      transition: theme.transitions.create('width', {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.enteringScreen,
      }),
 
      ...(!drawerOpen && {
        overflowX: 'hidden',
        transition: theme.transitions.create('width', {
          easing: theme.transitions.easing.sharp,
          duration: theme.transitions.duration.leavingScreen,
        })
      }),
    },
    '& .EveliFooter-root': {
      height: `${footerHeight}px`
    },

    "& .MuiAppBar-root.EveliShellBase": {
      display: 'flex',
      flexDirection: 'column',
      zIndex: theme.zIndex.drawer + 1,
      width: '100%',
      backgroundColor: theme.palette.background.paper,
      boxShadow: `0 0 7px ${theme.palette.text.disabled}`,
      height: toolbarHeight + 'px',
      paddingLeft: theme.spacing(1),
      paddingRight: theme.spacing(1),
    },

    '& .MuiAppBar-root.EveliShellBase .MuiStack-root': {
      height: '100%',
      alignItems: 'center'
    },
    "& main": {
      flex: 1,
      marginLeft: `${drawerWidth}px`,
      width: `calc(100% - ${drawerWidth}px)`,
      minHeight: '100vh',
      backgroundColor: theme.palette.background.paper,
      marginTop: `${toolbarHeight}px`,
    },
    "& .MuiContainer-root": {
      overflow: 'auto',
      maxWidth: 'none',
      padding: 'unset',
    }
  };
});