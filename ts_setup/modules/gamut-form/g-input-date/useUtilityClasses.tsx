import { generateUtilityClass, styled } from '@mui/material'
import composeClasses from '@mui/utils/composeClasses'
import { useVariantOverride } from '@dxs-ts/gamut-api';

export const MUI_NAME = 'GInputDate';

export const useUtilityClasses = (itemId: string, variant: string | undefined) => {
  const slots = { root: ['root', variant, itemId], input: ['input'] };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

interface OwnerState {
  variant: string;
  disabled: boolean;
  readOnly?: boolean;
}

export const GInputDateRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => [styles.root, useVariantOverride(props, styles)],
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => {

  if (ownerState.readOnly) {
    return {
      '& .MuiOutlinedInput-notchedOutline': { border: 'none' },
      '& .MuiOutlinedInput-root': { backgroundColor: 'transparent' },
      '& .MuiInputBase-input': {
        cursor: 'not-allowed',
        color: theme.palette.text.primary,
        ...theme.typography.body1,
      },
    }
  }

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
