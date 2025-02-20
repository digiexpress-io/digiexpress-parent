
import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';


export const MUI_NAME = 'EveliLocales';

export interface EveliLocalesClasses {
  root: string;
  selectedLocale: string;
}
export type EveliLocalesClassKey = keyof EveliLocalesClasses;


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    selectedLocale: ['selectedLocale'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const EveliLocalesRoot = styled('span', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.selectedLocale
    ];
  },
})(({ theme }) => {
  return {
    ' .EveliLocales-selectedLocale': {

    }
  };
});

