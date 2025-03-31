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

paddingTop: theme.spacing(1),
paddingRight: theme.spacing(1)

}));
