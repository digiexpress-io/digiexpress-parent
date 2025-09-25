import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useVariantOverride } from '@dxs-ts/gamut-api';

export const MUI_NAME = 'GInputTime';

export interface GInputTimeClasses {
  root: string;
  inputContainer: string;
  input: string;
  endAdornment: string;
  clearButton: string;
  timeButton: string;
}

export type GInputTimeClassKey = keyof GInputTimeClasses;

export const useUtilityClasses = (itemId: string, variant: string | undefined): GInputTimeClasses => {
  const slots = {
    root: ['root', variant, itemId],
    inputContainer: ['inputContainer'],
    input: ['input'],
    endAdornment: ['endAdornment'],
    clearButton: ['clearButton'],
    timeButton: ['timeButton'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {}) as GInputTimeClasses;
};

export const GInputTimeRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => [styles.root, useVariantOverride(props, styles)],
})<{ ownerState: { variant: string; disabled: boolean } }>(({ ownerState }) => {
  if (ownerState.disabled) {
    return {
      '& .MuiSvgIcon-root': { display: 'none' },
    };
  }
  return {};
});

export const GInputTimeInputContainer = styled('div', {
  name: MUI_NAME,
  slot: 'InputContainer',
  overridesResolver: (props, styles) => [styles.inputContainer, styles.input, useVariantOverride(props, styles)],
})<{ ownerState: { variant: string } }>(({ theme }) => ({
  '& input[type="time"]::-webkit-calendar-picker-indicator': { display: 'none' },
  '& input[type="time"]::-webkit-clear-button': { display: 'none' },
  '& input[type="time"]::-webkit-inner-spin-button': { display: 'none' },
  '& input[type="time"]::-webkit-outer-spin-button': { display: 'none' },
  '& input[type="time"]::-ms-clear': { display: 'none' },
  '& input[type="time"]::-ms-reveal': { display: 'none' },
  '& input[type="time"]': { MozAppearance: 'textfield' as any },

  '& .MuiInputAdornment-root .MuiIconButton-root + .MuiIconButton-root': {
    marginLeft: theme.spacing(0.5),
  },
}));
