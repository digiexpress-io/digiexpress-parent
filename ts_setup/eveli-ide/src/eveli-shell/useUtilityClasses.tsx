import { styled, generateUtilityClass, alpha } from '@mui/material'
import composeClasses from '@mui/utils/composeClasses'


export const EveliShellClassName = 'EveliShellBase';
export const EveliShellMiniBarClassName = 'EveliShellMiniBar';
export const EveliShellLargeBarClassName = 'EveliShellLargeBar';
export const EveliShellMiniBarTopClassName = 'EveliShellMiniBarTop';

export const MUI_NAME = 'EveliShell';
export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    unsaved: ['unsaved'],
    itemDisabled: ['itemDisabled'],
    itemActive: ['itemActive'],
    textActive: ['textActive'],
    logoContainer: ['logoContainer'],
    logo: ['logo'],
    composeButton: ['composeButton'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

export const EveliShellRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.unsaved,
      styles.itemDisabled,
      styles.itemActive,
      styles.textActive,
      styles.logoContainer,
      styles.logo,
      styles.composeButton,
    ];
  },
  shouldForwardProp: (prop) => prop !== 'toolbarHeight' && prop !== 'ownerState',
})<{
  ownerState: {
    toolbarHeight: number;
    footerHeight: number;
    drawerWidth: number;
    drawerOpen: boolean;
    minibarWidth: number
  }
}>(({ theme, ownerState }) => {

  const drawerWidth = ownerState.drawerOpen ? ownerState.drawerWidth : ownerState.minibarWidth;

  const {
    toolbarHeight,
    footerHeight,
    drawerOpen,
  } = ownerState;

  return {
    display: 'flex',
    flexDirection: 'column',

    '& .EveliShellMiniBarTop': {
      display: 'flex',
      alignItems: 'center',
      flexDirection: 'column'
    },

    '& .EveliShellMiniBar': {
      display: 'flex',
      flexDirection: 'column',
      textAlign: 'center',
      width: `${ownerState.minibarWidth - 1}px`,
      borderRight: drawerOpen ? `1px solid ${theme.palette.divider}` : undefined,
      backgroundColor: theme.palette.secondary.dark
    },

    '& .EveliShellMiniBar > div': {
      marginBottom: theme.spacing(1),
    },

    '& .EveliShellMiniBar .MuiTypography-root': {
      ...theme.typography.caption
    },

    "& .EveliShellMiniBar .Mui-selected": {
      color: theme.palette.secondary.contrastText,
    },

    "& .EveliAppBar-root": {
      position: 'sticky',
      top: '0',
      zIndex: theme.zIndex.drawer + 1,
    },

    '& .EveliShell-logoContainer': {
      display: 'flex',
      justifyContent: 'center',
    },

    '& .EveliShell-logo': {
      height: '45px',
      width: '160px',
      marginBottom: theme.spacing(2)
    },

    '& .EveliShell-composeButton': {
      backgroundColor: theme.palette.background.default,
      borderRadius: theme.spacing(2),
      color: theme.palette.text.secondary,
      width: '100%',
      ...theme.typography.body1,
      fontWeight: 'bold',
      padding: theme.spacing(2),
      marginBottom: theme.spacing(3),
      ':hover': {
        backgroundColor: theme.palette.background.default
      },
    },
    '& .EveliShellBase .MuiDrawer-paper': {
      backgroundColor: theme.palette.secondary.main,
      boxSizing: 'border-box',
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
      marginLeft: `${drawerWidth}px`,
      height: `${footerHeight}px`
    },

    "& .MuiAppBar-root.EveliShellBase": {
      display: 'flex',
      width: '100%',
      backgroundColor: theme.palette.background.paper,
      boxShadow: `0 0 7px ${theme.palette.text.disabled}`,
      height: toolbarHeight + 'px',

      paddingLeft: `calc(${drawerWidth}px + ${theme.spacing(1)})`,
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
    },
  };
});



export const EveliShellMiniBarRoot = styled('div', {
  name: MUI_NAME,
  slot: 'MiniBar',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.unsaved
    ];
  },
  shouldForwardProp: (prop) => prop !== 'toolbarHeight' && prop !== 'ownerState',
})<{ ownerState: { unsaved?: boolean } }>(({ theme, ownerState }) => {

  return {

    '& .EveliShell-unsaved': {
      color: ownerState.unsaved ? theme.palette.common.black : theme.palette.text.secondary,
      backgroundColor: alpha(theme.palette.warning.main, 0.8),
      padding: theme.spacing(1)
    },
    '& .EveliShell-itemDisabled': {
      color: theme.palette.action.disabled
    },
    '& .EveliShell-itemActive': {
      color: theme.palette.secondary.main,
      backgroundColor: theme.palette.primary.main,
      padding: theme.spacing(1),
    },
    '& .EveliShell-textActive': {
      color: theme.palette.primary.main,
    }
  }
})