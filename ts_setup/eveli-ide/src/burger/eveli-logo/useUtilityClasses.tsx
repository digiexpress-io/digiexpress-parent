import { generateUtilityClass, styled, Theme } from '@mui/material';
import { EveliLogoProps } from './EveliLogo';
import { unstable_createStyleFunctionSx } from '@mui/system';
import composeClasses from '@mui/utils/composeClasses';


export const MUI_NAME = 'EveliLogo';
export interface EveliLogoClasses {
  root: string;
}
export type EveliLogoClassKey = keyof EveliLogoClasses;


export const EveliLogoRoot = styled('img', {
  name: MUI_NAME,
  slot: 'Root',
  skipSx: false,
  overridesResolver: (props, styles) => {
    return [
      styles.root,
    ];
  },
})<{ ownerState: EveliLogoProps }>(({ theme, ownerState }) => {
  // get the 'style' override
  const target = getVariant(theme, ownerState.variant);

  // convert theme "style" property to CSS
  const sx = unstable_createStyleFunctionSx({})({ theme, sx: target?.style })
  return {
    //backgroundRepeat: 'no-repeat',
    marginTop: theme.spacing(1),
    marginBottom: theme.spacing(1),
    ...sx,

  };
});


export const useUtilityClasses = (ownerState: EveliLogoProps) => {
  const slots = {
    root: ['root']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export function getVariant(theme: Theme, variant: string | undefined) {
  const variants = theme.components?.EveliLogo?.variants;
  const target = variants?.find(({ props }) => props.variant === variant);
  return target;
}