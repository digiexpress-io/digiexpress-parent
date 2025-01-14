import { Breadcrumbs, generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";


export const MUI_NAME = 'GRouterSecuredServices';


export interface GRouterSecuredServicesClasses {
  root: string;
}
export type GRouterSecuredServicesClassKey = keyof GRouterSecuredServicesClasses;


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GRouterSecuredServicesRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
    ];
  },
})(({ theme }) => {
  return {
  }
});


export const GRouterSecuredServicesBreadcrumbsRoot = styled(Breadcrumbs, {
  name: MUI_NAME,
  slot: 'BreadcrumbsRoot',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },
})(({ theme }) => {
  return {

  }
});




