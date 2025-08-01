import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';



export const MUI_NAME = 'GAuthRepPerson';

export interface GAuthRepPersonClasses {
  root: string;
}

export type GAuthRepPersonClassKey = keyof GAuthRepPersonClasses;


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

export const GAuthRepPersonRoot = styled("div", {
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

