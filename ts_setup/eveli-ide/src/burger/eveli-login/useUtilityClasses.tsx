import { styled, generateUtilityClass } from '@mui/material';
import { EveliLoginProps } from './EveliLogin';
import composeClasses from '@mui/utils/composeClasses';

export const MUI_NAME = 'EveliLogin'

export interface EveliLoginClasses {
  root: string;
}
export type EveliLoginClassKey = keyof EveliLoginClasses;

export const EveliLoginRoot = styled('div', {
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

export const useUtilityClasses = (ownerState: EveliLoginProps) => {
  const slots = { root: ['root'] };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
