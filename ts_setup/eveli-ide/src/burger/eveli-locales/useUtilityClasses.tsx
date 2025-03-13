
import { generateUtilityClass, Popover, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';


export const MUI_NAME = 'EveliLocales';

export interface EveliLocalesClasses {
  root: string;
}
export type EveliLocalesClassKey = keyof EveliLocalesClasses;


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    languageSelect: ['languageSelect']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const EveliLocalesRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.languageSelect
    ];
  },
})(({ theme }) => {
  return {
  };
});

export const EveliLocalesLanguageSelect = styled(Popover, {
  name: MUI_NAME,
  slot: 'LanguageSelect',
  overridesResolver: (_props, styles) => {
    return [
    ];
  },
})(({ theme }) => {
  return {
    '& .MuiPaper-root': {
      minWidth: 200
    },
    '& p:first-of-type': {
      fontWeight: 'bold',
      padding: theme.spacing(2)
    },
  };
});
