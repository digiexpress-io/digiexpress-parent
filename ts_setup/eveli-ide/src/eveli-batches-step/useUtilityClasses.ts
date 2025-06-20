import { generateUtilityClass, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";

export const MUI_NAME = 'EveliBatchStep';

export type BatchViewHeadersClassKey = keyof BatchViewHeadersClasses;

export interface BatchViewHeadersClasses {
  root: string;
  stepSection: string;
}

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};


export const EveliBatchStepRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root
    ];
  },
})<{ ownerState: {} }>(({ theme }) => {
  return {
 

  }
})