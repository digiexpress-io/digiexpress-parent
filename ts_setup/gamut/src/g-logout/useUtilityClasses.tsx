import { styled, generateUtilityClass } from '@mui/material'
import { GLogoutProps } from './GLogout';
import composeClasses from '@mui/utils/composeClasses';

export const MUI_NAME = 'GLogout';


export interface GLogoutClasses {
  root: string;
}
export type GLogoutClassKey = keyof GLogoutClasses;

export const GLogoutRoot = styled('div', {
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

export const useUtilityClasses = (ownerState: GLogoutProps) => {
  const slots = { root: ['root'] };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}