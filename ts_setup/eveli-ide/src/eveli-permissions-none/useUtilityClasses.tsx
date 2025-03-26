import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';


export const MUI_NAME = 'EveliPermissionsNone';
export interface EveliPermissionsNoneClasses {
  root: string;
  logoBox: string;
}

export type EveliPermissionsNoneClassKey = keyof EveliPermissionsNoneClasses;


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    logoBox: ['logoBox']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const EveliPermissionsNoneRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (styles) => {
    return [
      styles.root,
      styles.logoBox,
    ];
  },
})(({ theme }) => {

  return {
    display: 'flex',
    border: `1px solid ${theme.palette.divider}`,
    padding: theme.spacing(5),

    [theme.breakpoints.up('md')]: {
      alignItems: 'center',
      marginTop: theme.spacing(10),
      marginRight: theme.spacing(5),
      marginLeft: theme.spacing(5),
      justifyContent: 'center',
    },

    [theme.breakpoints.down('md')]: {
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'flex-start',
      '.MuiTypography-root': {
        marginTop: theme.spacing(1)
      }
    },

    '.EveliPermissionsNone-logoBox': {
      height: '100%',
      display: 'flex',
      [theme.breakpoints.up('md')]: {
        width: '25%',
      }
    }

  }
});