import { styled, generateUtilityClass } from '@mui/material'
import composeClasses from '@mui/utils/composeClasses';

export const MUI_NAME = 'GSort';


export interface GSortClasses {
  root: string;
  direction: string;
}
export type GSortClassKey = keyof GSortClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const GSortRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => [
    styles.root,
  ],
})(({ theme }) => ({
  display: 'flex',
  width: '100%',
  paddingTop: theme.spacing(1),

  [theme.breakpoints.down('sm')]: {
    justifyContent: 'center',
  },
  [theme.breakpoints.up('sm')]: {
    justifyContent: 'flex-end',
    paddingRight: theme.spacing(1),
  },
}));
