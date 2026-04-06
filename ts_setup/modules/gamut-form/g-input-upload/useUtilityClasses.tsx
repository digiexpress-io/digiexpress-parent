import { generateUtilityClass, styled } from '@mui/material'
import composeClasses from "@mui/utils/composeClasses";
import { useVariantOverride } from '@dxs-ts/gamut-api'


export const MUI_NAME = 'GInputUpload';

interface OwnerState {
  variant: string;
  disabled: boolean;
  readOnly?: boolean;
}

// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
export const GInputUploadRoot = styled("div", {
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
      '& .MuiInputLabel-root.Mui-disabled': {
        color: theme.palette.info.main,
      },
      '& .MuiInputBase-input.Mui-disabled': {
        WebkitTextFillColor: theme.palette.info.main,
      },
      '& .MuiSvgIcon-root': {
        display: 'none'
      }
    }
  }

  if (ownerState.readOnly) {
    return {
      '& .MuiSvgIcon-root': {
        display: 'none'
      }
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