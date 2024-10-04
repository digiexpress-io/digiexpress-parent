import { alpha, CircularProgress, generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import { useVariantOverride } from '../api-variants';
import { OwnerState } from './GForm';



export const MUI_NAME = 'GForm';

export interface GFormClasses {
  root: string;
  variant: string;
}
export type GFormClassKey = keyof GFormClasses;



export const useUtilityClasses = (ownerState: OwnerState) => {
  const slots = {
    root: ['root', ownerState.variant],
    progress: ['progress', ownerState.variant]
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GFormRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      ...useVariantOverride(props, styles),
    ];
  },
})<{ ownerState: OwnerState }>(({ theme }) => {
  return {
    // input element inside
    '& .MuiInputBase-input': {
      paddingTop: theme.spacing(1),
      paddingBottom: theme.spacing(1),
    },
    '& .MuiInputBase-root': {
      backgroundColor: alpha(theme.palette.warning.main, 0.1),
      paddingLeft: theme.spacing(1),
      paddingRight: theme.spacing(1),
    },
    '& .MuiAutocomplete-inputRoot': {
      padding: 'unset!important',
      paddingLeft: `${theme.spacing(2)}!important`,
      paddingRight: `${theme.spacing(2)}!important}`
    },


    '& .GFormPage-root': {
      backgroundColor: theme.palette.background.default,
      border: `1px solid ${theme.palette.divider}`,
      borderRadius: theme.spacing(1),

      paddingLeft: theme.spacing(5),
      paddingRight: theme.spacing(5),

      [theme.breakpoints.up('md')]: {
        marginLeft: theme.spacing(5),
        marginRight: theme.spacing(5),
      },
      [theme.breakpoints.up('lg')]: {
        marginLeft: theme.spacing(30),
        marginRight: theme.spacing(30),
      },
    },
    '& .GFormPage-body': {
      paddingTop: theme.spacing(2)
    },
    '& .GFormPage-header': {
      paddingTop: theme.spacing(5),
    },


    '& .GFormGroup-root': {
      paddingTop: theme.spacing(0),
      paddingBottom: theme.spacing(0)
    },
    '& .GFormGroup-label': {
      paddingTop: theme.spacing(1),
      paddingBottom: theme.spacing(1)
    },


    '& .GInputGroupRow-root': {
      paddingTop: theme.spacing(0),
      paddingBottom: theme.spacing(0)
    },
    '& .GInputGroupRow-label': {
      paddingTop: theme.spacing(1),
      paddingBottom: theme.spacing(1)
    },

    '& .GInputGroup-root': {
      paddingTop: theme.spacing(0),
      paddingBottom: theme.spacing(0)
    },
    '& .GInputGroup-label': {
      paddingTop: theme.spacing(1),
      paddingBottom: theme.spacing(1)
    },


    '& .GInputBase-root': {
      paddingTop: theme.spacing(1),
      paddingBottom: theme.spacing(1),
    },
    '& .GInputBase-label': {
      alignItems: 'center',
      display: 'flex',
      [theme.breakpoints.up('md')]: {
        justifyContent: 'right',
      }
    },

    // inside paddings
    '& .GInputMultilist-option': {
      paddingLeft: theme.spacing(2),
      paddingRight: theme.spacing(2),
    },
    // inside paddings
    '& .GInputBoolean-option': {
      paddingLeft: theme.spacing(2),
      paddingRight: theme.spacing(2),
    },


    '& .react-date-picker__inputGroup': {
      paddingLeft: theme.spacing(2) + '!important',
      paddingRight: theme.spacing(2) + '!important',
    },
    '& .react-time-picker__inputGroup': {
      paddingLeft: theme.spacing(2) + '!important',
      paddingRight: theme.spacing(2) + '!important',
    },

  };
});



export const GFormProgress = styled(CircularProgress, {
  name: MUI_NAME,
  slot: 'Progress',
  overridesResolver: (props, styles) => {
    return [
      styles.loader,
    ];
  },
})(({ theme }) => {
  return {

  };
});