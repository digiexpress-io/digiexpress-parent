import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { GAuthUnRepPersonProps } from './GAuthUnRepPerson';


export const MUI_NAME = 'GAuthRepPerson';

export interface GAuthUnRepPersonClasses {
  root: string;
}

export type GAuthUnRepPersonClassKey = keyof GAuthUnRepPersonClasses;


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

export const GAuthUnRepPersonRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  //shouldForwardProp: (propName) => ,
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },
})<GAuthUnRepPersonProps>(({ theme }) => {
  return {
  
  };
});

