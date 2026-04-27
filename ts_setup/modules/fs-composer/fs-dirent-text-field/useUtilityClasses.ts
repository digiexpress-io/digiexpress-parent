import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';
import { OwnerState } from './useOwnerState';

const MUI_NAME = 'FsDirentTextField';

export interface FsDirentTextFieldClasses {
  root: string;
  requiredMessage: string;
}

export type FsDirentTextFieldClassKey = keyof FsDirentTextFieldClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    requiredMessage: ['requiredMessage'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentTextFieldRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => {
  const dangerColor    = ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight;
  const borderColor    = ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border;
  const hoverColor     = ownerState.isDarkMode ? FsColors.light.textSecondary : FsColors.dark.textSecondary;
  const focusColor     = ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text;
  const textColor      = ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text;
  const bgColor        = ownerState.isDarkMode ? FsColors.dark.background : FsColors.light.background;
  const placeholderColor = ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary;

  const activeBorderColor  = ownerState.showRequiredError ? dangerColor : borderColor;
  const activeHoverColor   = ownerState.showRequiredError ? dangerColor : hoverColor;
  const activeFocusBorder  = ownerState.showRequiredError ? `1px solid ${dangerColor}` : `1px solid ${focusColor}`;

  return {
    width: '100%',
    marginTop: '0px !important',

    '& .MuiFormControl-root': {
      width: '100%',
      marginTop: '0 !important',
      marginBottom: '0 !important',
    },

    '& .MuiInputBase-root': {
      backgroundColor: bgColor,
      color: textColor,
      borderRadius: 0,

      '& fieldset': {
        borderColor: activeBorderColor,
        borderRadius: 0,
      },
      '&:hover fieldset': {
        borderColor: activeHoverColor,
      },
      '&.Mui-disabled': {
        pointerEvents: 'none',
      },
      '&.Mui-focused fieldset': {
        border: activeFocusBorder,
      },
      '&.Mui-focused:has(:is(input, textarea):placeholder-shown) fieldset': {
        border: ownerState.isRequired ? `2px solid ${dangerColor}` : activeFocusBorder,
      },
    },

    '& .MuiInputBase-input': {
      color: textColor,
      ...theme.typography.caption,
      padding: theme.spacing(1.5),

      '&::placeholder': {
        color: placeholderColor,
        opacity: 1,
        ...theme.typography.caption,
      },

      '&.Mui-disabled': {
        WebkitTextFillColor: placeholderColor,
      },
    },

    '& .MuiInputBase-input.MuiOutlinedInput-input.MuiInputBase-inputMultiline': {
      padding: 'unset',
    },

    '& .MuiInputLabel-root': {
      color: textColor,
      ...theme.typography.caption,
    },

    [`& .${MUI_NAME}-requiredMessage`]: {
      ...theme.typography.caption,
      display: ownerState.showRequiredError ? 'block' : 'none',
      color: dangerColor,
      marginTop: '3px',
      marginLeft: 0,
      '&.MuiTypography-root': {
        color: dangerColor,
      },
    },
  };
});
