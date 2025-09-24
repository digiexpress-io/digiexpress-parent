import { alpha, generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useVariantOverride } from '@dxs-ts/eveli-api';


export const MUI_NAME = 'EveliAlert';
export interface EveliAlertClasses {
  root: string;
}

export type EveliAlertClassKey = keyof EveliAlertClasses;


export const useUtilityClasses = () => {
  const slots = {
    root: ['root']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const EveliAlertRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      ...useVariantOverride(props, styles)
    ];
  },
})(({ theme }) => {
  return {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',

    '& .MuiAlert-root': {
      border: `1px solid ${theme.palette.primary.main}`,
      backgroundColor: alpha(theme.palette.primary.main, 0.05),
      color: theme.palette.primary.main,
      borderRadius: 'unset'
    },
    '& .MuiAlert-root .MuiSvgIcon-root': {
      size: 'large',
      color: theme.palette.primary.main
    }
  };
});