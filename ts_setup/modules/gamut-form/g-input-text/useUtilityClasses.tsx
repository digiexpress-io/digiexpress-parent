import { generateUtilityClass, styled } from '@mui/material'
import composeClasses from "@mui/utils/composeClasses";
import { useVariantOverride } from '@dxs-ts/gamut-api'


export const MUI_NAME = 'GInputText';

interface OwnerState {
  variant: string,
  disabled: boolean,
  readOnly?: boolean
}


// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
export const GInputTextRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      useVariantOverride(props, styles)
    ];
  },
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => {

  if (ownerState.disabled) {
    return {

      '& .MuiInputBase-input.Mui-disabled': {
        WebkitTextFillColor: theme.palette.info.main,
      },
      '& .MuiOutlinedInput-root.Mui-disabled': {
        backgroundColor: theme.palette.background.paper,
      }
    }
  }

  if (ownerState.readOnly) {
    return {
      '& .MuiOutlinedInput-notchedOutline': {
        border: 'none',
      },
      '& .MuiOutlinedInput-root': {
        backgroundColor: 'transparent',
      },
      '& .MuiInputBase-input': {
        cursor: 'not-allowed',
        color: theme.palette.text.primary,
        ...theme.typography.body1,
      },
    }
  }

});


// ------------------- MATERIAL INFRA, CSS CLASS NAMES FOR SELECTORS -------
export const useUtilityClasses = (itemId: string, variant: string | undefined) => {
  const slots = {
    root: ['root', variant, itemId],
    input: ['input']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}