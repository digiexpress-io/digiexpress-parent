import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';


export const MUI_NAME = 'GAuthRepCompany';

export interface GAuthRepCompanyClasses {
  root: string;
}

export type GAuthRepCompanyClassKey = keyof GAuthRepCompanyClasses;


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

export const GAuthRepCompanyRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  //shouldForwardProp: (propName) => ,
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },
})(({ theme }) => {
  return {
  
  };
});

