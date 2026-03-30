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
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => ({
  width: '100%',
  marginTop: '0px !important',
  '& .MuiFormControl-root': {
    width: '100%',
    marginTop: '0 !important',
    marginBottom: '0 !important',
  },
  '& .MuiInputBase-root': {
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.background : FsColors.light.background,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    borderRadius: 0,
    '& fieldset': {
      borderColor: ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border,
      borderRadius: 0,
    },
    '&:hover fieldset': {
      borderColor: ownerState.isDarkMode ? FsColors.light.textSecondary : FsColors.dark.textSecondary,
    },
    '&.Mui-disabled:hover fieldset': {
      borderColor: ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border,
    },
    '&.Mui-focused fieldset': {
      border: `1px solid ${ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text}`,
    },
    '&.Mui-focused:has(:is(input, textarea):placeholder-shown) fieldset': {
      border: `2px solid ${ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight}`,
    },
  },
  '& .MuiInputBase-input': {
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    ...theme.typography.caption,
    padding: theme.spacing(1.5),

    '&::placeholder': {
      color: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
      opacity: 1,
      ...theme.typography.caption,
    },
  },
  '& .MuiInputBase-input.MuiOutlinedInput-input.MuiInputBase-inputMultiline': {
    padding: 'unset',
  },
  '& .MuiInputLabel-root': {
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    ...theme.typography.caption,
  },
  ...(ownerState.showRequiredError && {
    '& .MuiOutlinedInput-root fieldset': {
      borderColor: ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight,
    },
    '& .MuiOutlinedInput-root:hover fieldset': {
      borderColor: ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight,
    },
    '& .MuiOutlinedInput-root.Mui-focused fieldset': {
      borderColor: ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight,
    },
  }),

  [`& .${MUI_NAME}-requiredMessage`]: {
    ...theme.typography.caption,
    visibility: ownerState.showRequiredError ? 'visible' : 'hidden',
    color: ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight,
    marginTop: '3px',
    marginLeft: 0,
    '&.MuiTypography-root': {
      color: ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight,
    },
  },
}));
