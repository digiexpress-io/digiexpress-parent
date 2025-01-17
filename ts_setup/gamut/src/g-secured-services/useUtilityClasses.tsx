import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { GSecuredServicesProps } from './GSecuredServices';

export const MUI_NAME = 'GSecuredServices';


export interface GSecuredServicesClasses {
  root: string;
  serviceLink: string;
}
export type GSecuredServicesClassKey = keyof GSecuredServicesClasses;

export const GSecuredServicesRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.serviceLink
    ];
  },
})<{ ownerState: GSecuredServicesProps }>(({ theme }) => {
  return {
    paddingLeft: theme.spacing(2),
    margin: theme.spacing(0.5),
    '.GServices-serviceLink': {

    }
  };
});


export const useUtilityClasses = (ownerState: GSecuredServicesProps) => {
  const slots = {
    root: ['root'],
    serviceLink: ['serviceLink']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
