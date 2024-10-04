import { styled, useThemeProps } from '@mui/material'
import composeClasses from '@mui/utils/composeClasses'
import generateUtilityClass from '@mui/utils/generateUtilityClass'
import { GInputAdornmentProps } from './GInputAdornment'
import { useVariantOverride } from '../api-variants';


const MUI_NAME = 'GInputAdornment';


export function useThemeInfra(initProps: GInputAdornmentProps) {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  })
  
  const ownerState = { ...props };
  
  const classes = useUtilityClasses(ownerState);
  return { classes, ownerState, props };
}


// ------------------- MATERIAL INFRA, CSS CLASS NAMES FOR SELECTORS -------
const useUtilityClasses = (ownerState: GInputAdornmentProps) => {
  const slots = {
    root: ['root', ownerState.id],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
export const GInputAdornmentRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      ...useVariantOverride(props, styles)
    ];
  },
})<{ ownerState: GInputAdornmentProps }>(({ theme }) => {
  return {
    paddingLeft: theme.spacing(1),
    display: 'flex',
    alignItems: 'center',
    '& .MuiIconButton-root': {
      padding: theme.spacing(0.5)
    }
  };
});
