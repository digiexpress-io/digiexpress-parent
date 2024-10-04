import { generateUtilityClass, styled, Theme } from '@mui/material';
import { GLogoProps } from './GLogo';
import { unstable_createStyleFunctionSx } from '@mui/system';
import composeClasses from '@mui/utils/composeClasses';


export const MUI_NAME = 'GLogoRoot';
export interface GLogoClasses {
  root: string;
}
export type GLogoClassKey = keyof GLogoClasses;


export const GLogoRoot = styled('img', {
  name: MUI_NAME,
  slot: 'Root',
  skipSx: false,
  overridesResolver: (props, styles) => {
    return [
      styles.root,
    ];
  },
})<{ ownerState: GLogoProps }>(({ theme, ownerState }) => {
  // get the 'style' override
  const target = getVariant(theme, ownerState.variant);

  // convert theme "style" property to CSS
  const sx = unstable_createStyleFunctionSx({})({ theme, sx: target?.style })
  return {
    //backgroundRepeat: 'no-repeat',
    ...sx
  };
});

export const useUtilityClasses = (ownerState: GLogoProps) => {
  const slots = {
    root: ['root']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export function getVariant(theme: Theme, variant: string | undefined) {
  const variants = theme.components?.GLogo?.variants;
  const target = variants?.find(({ props }) => props.variant === variant);

  return target;
}