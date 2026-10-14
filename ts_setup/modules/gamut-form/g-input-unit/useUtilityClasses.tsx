import { generateUtilityClass, styled } from '@mui/material'
import composeClasses from "@mui/utils/composeClasses";
import { useVariantOverride } from '@dxs-ts/gamut-api'


export const MUI_NAME = 'GInputUnit';

interface OwnerState {
  variant: string,
  disabled: boolean,
  readOnly?: boolean
}


export const GInputUnitRoot = styled("div", {
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
      '& .MuiOutlinedInput-root.Mui-disabled': {
        backgroundColor: theme.palette.background.paper,
      },
      '& .MuiInputAdornment-positionEnd': {
        marginRight: theme.spacing(1),
      },
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

  return {
    '& .MuiInputAdornment-positionEnd': {
      marginRight: theme.spacing(1),
    }
  };

});


export const useUtilityClasses = (itemId: string, variant: string | undefined) => {
  const slots = {
    root: ['root', variant, itemId],
    input: ['input']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
