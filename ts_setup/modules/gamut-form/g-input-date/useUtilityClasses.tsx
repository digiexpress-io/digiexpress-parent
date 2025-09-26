import { generateUtilityClass, styled } from '@mui/material'
import composeClasses from '@mui/utils/composeClasses'
import { useVariantOverride } from '@dxs-ts/gamut-api';

export const MUI_NAME = 'GInputDate';

export const useUtilityClasses = (itemId: string, variant: string | undefined) => {
  const slots = { root: ['root', variant, itemId], input: ['input'] };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

export const GInputDateRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => [styles.root, useVariantOverride(props, styles)],
})<{ ownerState: { variant: string, disabled: boolean } }>(({ theme, ownerState }) => {
  if (ownerState.disabled) {
    return {
      '& .MuiSvgIcon-root': { display: 'none' }
    };
  }
  return {
    '.MuiPaper-root': {
      backgroundColor: 'red'
    }
  };
});

export const GInputDateInput = styled('div', {
  name: MUI_NAME,
  slot: 'Input',
  overridesResolver: (props, styles) => [styles.root, useVariantOverride(props, styles)],
})<{ ownerState: { variant: string } }>(({ theme }) => {
  return {
    '& .XuiDatePicker-input': {
      border: 'unset',
      borderRadius: 'unset',
      paddingLeft: theme.spacing(2),
      '&:focus-within': {
        border: `2px solid ${theme.palette.primary.main}`,
        borderRadius: theme.spacing(0.5)
      }
    }

  };
});
