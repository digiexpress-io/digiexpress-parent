
import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';


export const MUI_NAME = 'GLocales';

export interface GLocalesClasses {
  root: string;
  selectedLocale: string;
}
export type GLocalesClassKey = keyof GLocalesClasses;


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    selectedLocale: ['selectedLocale'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GLocalesRoot = styled('span', {
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
    ' .GLocales-selectedLocale': {

    }
  };
});

