import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { GServicesProps } from './GServices';

export const MUI_NAME = 'GServices';


export interface GServicesClasses {
  root: string;
  serviceLink: string;
}
export type GServicesClassKey = keyof GServicesClasses;

export const GServicesRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.serviceLink
    ];
  },
})<{ ownerState: GServicesProps }>(({ theme }) => {
  return {
    paddingLeft: theme.spacing(2),
    margin: theme.spacing(0.5),
    '.GServices-serviceLink': {

    }
  };
});


export const useUtilityClasses = (ownerState: GServicesProps) => {
  const slots = {
    root: ['root'],
    serviceLink: ['serviceLink']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
