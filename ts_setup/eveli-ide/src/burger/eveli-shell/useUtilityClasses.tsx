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
    logoContainer: ['logoContainer'],
    logo: ['logo'],
    composeButton: ['composeButton'],
    menuButton: ['menuButton'],
    menuButtonActive: ['menuButtonActive'],
    secondaryDivider: ['secondaryDivider'],
    logoutButton: ['logoutButton']
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
      styles.logoContainer,
      styles.logo,
      styles.composeButton,
      styles.menuButton,
      styles.menuButtonActive,
      styles.secondaryDivider,
      styles.logoutButton
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
  const drawerWidth = ownerState.drawerOpen ? ownerState.drawerWidth : minibarWidth;
  const largebarWidth = ownerState.drawerOpen ? drawerWidth - minibarWidth : 0;

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

    '& .EveliShellMiniBarTop': {
      display: 'flex',
      alignItems: 'center',
      flexDirection: 'column'
    },

    '& .EveliShellMiniBar': {
      display: 'flex',
      flexDirection: 'column',
      textAlign: 'center',
      width: `${minibarWidth - 1}px`,
      borderRight: drawerOpen ? `1px solid #CED8DE` : undefined,
      backgroundColor: 'rgb(236, 239, 243)',
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

    '& .EveliShellLargeBar': {
      width: `${largebarWidth - 1}px`,
      display: drawerOpen ? undefined : 'none',
      padding: theme.spacing(1)
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
      backgroundColor: '#FFFFFF',
      borderRadius: theme.spacing(2),
      color: 'rgb(58, 55, 55)',
      width: '100%',
      ...theme.typography.body1,
      fontWeight: 'bold',
      padding: theme.spacing(2),
      marginBottom: theme.spacing(3),
      ':hover': {
        backgroundColor: '#FFFFFF'
      },
    },

    '& .EveliShell-menuButton': {
      justifyContent: 'left',
      marginTop: theme.spacing(0.5),
      borderRadius: theme.spacing(3),
      paddingLeft: theme.spacing(2),
      border: `1px solid rgb(246, 249, 253)`,
      ...theme.typography.body1,
      color: 'rgb(58, 55, 55)',
      width: '100%',
      ':hover': {
        backgroundColor: 'rgb(236, 239, 243)',
        border: `1px solid rgb(246, 249, 253)`,
      }
    },

    '& .EveliShell-menuButtonActive': {
      justifyContent: 'left',
      marginTop: theme.spacing(0.5),
      borderRadius: theme.spacing(3),
      border: `1px solid #CED8DE`,
      ...theme.typography.body1,
      paddingLeft: theme.spacing(2),
      fontWeight: 'bold',
      color: 'rgb(58, 55, 55)',
      width: '100%',
      backgroundColor: 'rgb(236, 239, 243)',
      ':hover': {
        backgroundColor: 'rgb(236, 239, 243)',
        border: `1px solid #CED8DE`,
      }
    },
    '& .EveliShell-logoutButton': {
      justifyContent: 'left',
      alignItems: "flex-start",
      marginTop: theme.spacing(0.5),
      borderRadius: theme.spacing(3),
      paddingLeft: theme.spacing(2),
      border: `1px solid rgb(246, 249, 253)`,
      ...theme.typography.body1,
      color: 'rgb(58, 55, 55)',
      width: '100%',
      ':hover': {
        backgroundColor: 'rgb(236, 239, 243)',
        border: `1px solid rgb(246, 249, 253)`,
      }
    },
    '& .EveliShell-secondaryDivider': {
      borderWidth: `1px solid #CED8DE`,
      marginTop: theme.spacing(1),
      marginBottom: theme.spacing(1)
    },

    '& .EveliShellBase .MuiDrawer-paper': {
      backgroundColor: 'rgb(246, 249, 253)',
      boxSizing: 'border-box',
      width: drawerWidth,

      display: 'flex',
      flexGrow: 1,
      flexDirection: 'row',

      borderRight: `1px solid #CED8DE`,

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
      width: '100%',
      backgroundColor: theme.palette.background.paper,
      boxShadow: `0 0 7px ${theme.palette.text.disabled}`,
      height: toolbarHeight + 'px',
      paddingLeft: drawerOpen ? theme.spacing(1) : theme.spacing(8),
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

export const EveliShellLargeBarRoot = styled('div', {
  name: MUI_NAME,
  slot: 'LargeBar',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
    ];
  },
})<{}>(({ theme }) => {

  return {


    '& .EveliShell-itemDisabled': {
      color: theme.palette.action.disabled
    }
  }
})


export const EveliShellMiniBarRoot = styled('div', {
  name: MUI_NAME,
  slot: 'MiniBar',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.unsaved
    ];
  },
})<{ ownerState: { unsaved?: boolean } }>(({ theme, ownerState }) => {

  return {

    '& .EveliShell-unsaved': {
      color: ownerState.unsaved ? theme.palette.common.black : 'rgb(58, 55, 55)',
      backgroundColor: alpha(theme.palette.warning.main, 0.8),
      padding: theme.spacing(1),
      '&:hover': {
        boxShadow: `0px 4px 6px rgba(0, 0, 0, 0.2)`
      }
    },
    '& .EveliShell-itemDisabled': {
      color: theme.palette.action.disabled
    }
  }
})