import { generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import { GLinksPageProps } from './GLinksPage';


export const MUI_NAME = 'GLinksPage';

export interface GLinksPageClasses {
  root: string;
}
export type GLinksPageClassKey = keyof GLinksPageClasses;


export const GLinksPageRoot = styled("div", {
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

export const useUtilityClasses = (ownerState: GLinksPageProps) => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
