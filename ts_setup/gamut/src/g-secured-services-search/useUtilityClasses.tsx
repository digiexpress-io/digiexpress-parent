
import { alpha, generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { GSecuredServicesSearchProps } from './GSecuredServicesSearch';



export const MUI_NAME = 'GSecuredServicesSearch';

export interface GSecuredServicesSearchClasses {
  root: string;
  icon: string;
  input: string;
}
export type GSecuredServicesSearchClassKey = keyof GSecuredServicesSearchClasses;


export const GSecuredServicesSearchRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.icon,
      styles.input
    ];
  },
})<{ ownerState: GSecuredServicesSearchProps }>(({ theme }) => {

  return {
    padding: theme.spacing(1),
    minHeight: '10vh',
    '.GSecuredServicesSearch-icon': {
      color: theme.palette.primary.main
    },
    '.GSecuredServicesSearch-input': {
    }
  };
});


export const useUtilityClasses = (ownerState: GSecuredServicesSearchProps) => {
  const slots = {
    root: ['root'],
    icon: ['icon'],
    input: ['input']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}