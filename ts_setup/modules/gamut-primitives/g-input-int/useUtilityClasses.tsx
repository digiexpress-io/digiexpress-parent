
import { generateUtilityClass, styled } from '@mui/material'
import composeClasses from '@mui/utils/composeClasses'
import { useVariantOverride } from '../g-variants';



export const MUI_NAME = 'GInputInt';


export const useUtilityClasses = (itemId: string, variant: string | undefined) => {
  const slots = {
    root: ['root', variant, itemId],


  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GInputIntRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      useVariantOverride(props, styles)
    ];
  },
})<{ ownerState: { variant: string, disabled: boolean } }>(({ theme, ownerState }) => {

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
});


