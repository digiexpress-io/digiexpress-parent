
import { alpha, generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { GServicesSearchProps } from './GServicesSearch';



export const MUI_NAME = 'GServicesSearch';

export interface GServicesSearchClasses {
  root: string;
  icon: string;
  input: string;
}
export type GServicesSearchClassKey = keyof GServicesSearchClasses;


export const GServicesSearchRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.icon,
      styles.input
    ];
  },
})<{ ownerState: GServicesSearchProps }>(({ theme }) => {

  return {

    padding: theme.spacing(2),
    marginBottom: theme.spacing(3),
    minHeight: '10vh',
    '.GServicesSearch-icon': {
      color: theme.palette.primary.main
    },
    '.GServicesSearch-input': {
      backgroundColor: alpha(theme.palette.warning.main, 0.1),
    }
  };
});


export const useUtilityClasses = (ownerState: GServicesSearchProps) => {
  const slots = {
    root: ['root'],
    icon: ['icon'],
    input: ['input']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}