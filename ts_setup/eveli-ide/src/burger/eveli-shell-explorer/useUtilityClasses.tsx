import { styled, generateUtilityClass, alpha } from '@mui/material'
import composeClasses from '@mui/utils/composeClasses'



export const MUI_NAME = 'EveliShellExplorer';

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const EveliShellExplorerRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
    ];
  },
})<{
    ownerState: {
      drawerOpen: boolean;
      drawerWidth: number;
      minibarWidth: number;
    }
}>(({ ownerState, theme }) => {

  const { drawerOpen, drawerWidth, minibarWidth } = ownerState;
  const largebarWidth = drawerOpen ? drawerWidth - minibarWidth : 0;

  return {
    width: `${largebarWidth - 1}px`,
    display: drawerOpen ? undefined : 'none',
    padding: theme.spacing(1),

    '& .MuiDivider-root': {
      borderWidth: `1px solid ${theme.palette.divider}`,
      marginTop: theme.spacing(1),
      marginBottom: theme.spacing(1)
    },
    '& .EveliShellExplorer-itemDisabled': {
      color: theme.palette.action.disabled
    },
    '& .EveliShellExplorer-logoutButton': {
      justifyContent: 'left',
      alignItems: "flex-start",
      marginTop: theme.spacing(0.5),
      borderRadius: theme.spacing(3),
      paddingLeft: theme.spacing(2),
      border: `1px solid ${theme.palette.secondary.main}`,
      ...theme.typography.body1,
      color: theme.palette.text.secondary,
      width: '100%',
      ':hover': {
        backgroundColor: theme.palette.secondary.dark,
        border: `1px solid ${theme.palette.secondary.main}`,
      }
    },
  }
})
