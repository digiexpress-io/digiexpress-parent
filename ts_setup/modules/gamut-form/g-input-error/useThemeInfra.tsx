import { styled, useThemeProps } from "@mui/material"
import composeClasses from '@mui/utils/composeClasses'
import generateUtilityClass from '@mui/utils/generateUtilityClass'

import { GInputErrorProps } from './GInputError'


const MUI_NAME = 'GInputError';


export function useThemeInfra(initProps: GInputErrorProps) {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  })
  

  const ownerState = {
    ...props,
  }
  
  const classes = useUtilityClasses(ownerState);
  return { classes, ownerState, props };
}

// ------------------- MATERIAL INFRA, CSS CLASS NAMES FOR SELECTORS -------
const useUtilityClasses = (ownerState: GInputErrorProps) => {
  const slots = {
    root: ['root', ownerState.id],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
export const GInputErrorRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.loader,
    ];
  },
})<{ ownerState: GInputErrorProps }>(({ theme }) => {
  return {

  };
});