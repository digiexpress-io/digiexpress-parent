import { styled, useThemeProps, Grid, alpha } from "@mui/material"
import composeClasses from '@mui/utils/composeClasses'
import generateUtilityClass from '@mui/utils/generateUtilityClass'
import { GInputBaseProps } from './GInputBase'
import { useVariantOverride } from "../api-variants"



const MUI_NAME = 'GInputBase';


export function useThemeInfra<T>(initProps: GInputBaseProps<T>) {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  })
  
  const ownerState = { ...props };
  const classes = useUtilityClasses(ownerState);
  return { classes, ownerState, props };
}

// ------------------- MATERIAL INFRA, CSS CLASS NAMES FOR SELECTORS -------
function useUtilityClasses<T>(ownerState: GInputBaseProps<T>) {
  const slots = {
    root: ['root', ownerState.id],
    label: ['label'],
    error: ['error'],
    input: ['input'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
export const GInputBaseRoot = styled(Grid, {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      styles.label,
      styles.error,
      styles.input,
      ...useVariantOverride(props, styles)
    ];
  },
})<{ ownerState: GInputBaseProps<any> }>(({ theme, ownerState }) => {
  return {
    '.GInputLabel-root': {
      paddingRight: ownerState.slotProps.adornment?.children ? 'unset' : undefined
    }
  };
})
