import { styled, generateUtilityClass } from '@mui/material';
import { GLoginProps } from './GLogin';
import composeClasses from '@mui/utils/composeClasses';

export const MUI_NAME = 'GLogin'

export interface GLoginClasses {
  root: string;
}
export type GLoginClassKey = keyof GLoginClasses;

export const GLoginRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
    ];
  },
})(({ theme }) => {
  return {

  };
});

export const useUtilityClasses = (ownerState: GLoginProps) => {
  const slots = { root: ['root'] };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
