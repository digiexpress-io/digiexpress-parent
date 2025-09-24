import { generateUtilityClass, styled } from '@mui/material'
import composeClasses from '@mui/utils/composeClasses'
import { useVariantOverride } from '@dxs-ts/gamut-api';

export const MUI_NAME = 'GInputTime';

export const useUtilityClasses = (itemId: string, variant: string | undefined) => {
  const slots = { root: ['root', variant, itemId], input: ['input'] };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

export const GInputTimeRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => [styles.root, useVariantOverride(props, styles)],
})<{ ownerState: { variant: string, disabled: boolean } }>(({ theme, ownerState }) => {
  if (ownerState.disabled) {
    return {
      '& .MuiSvgIcon-root': { display: 'none' }
    };
  }
  return {};
});

export const GInputTimeInput = styled("div", {
  name: MUI_NAME,
  slot: 'Input',
  overridesResolver: (props, styles) => [styles.root, useVariantOverride(props, styles)],
})<{ ownerState: { variant: string } }>(() => {
  return {};
});
