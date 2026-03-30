import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';
import { OwnerState } from './useOwnerState';

const MUI_NAME = 'FsDirentTextFieldAutocomplete';

export interface FsDirentTextFieldAutocompleteClasses {
  root: string;
}

export type FsDirentTextFieldAutocompleteClassKey = keyof FsDirentTextFieldAutocompleteClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentTextFieldAutocompleteRoot = styled('div', {
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
  '& .MuiOutlinedInput-root': {
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.background : FsColors.light.background,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    borderRadius: 0,
    minHeight: '50px',
    display: 'flex',
    alignItems: 'center',
    padding: theme.spacing(1),

    '& fieldset': {
      borderColor: ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border,
      borderRadius: 0,
    },
    '&:hover fieldset': {
      borderColor: ownerState.isDarkMode ? FsColors.light.textSecondary : FsColors.dark.textSecondary,
    },
    '&.Mui-focused fieldset': {
      border: `1px solid ${ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text}`,
    },
  },

  '& .MuiInputBase-input': {
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    ...theme.typography.caption,
    padding: theme.spacing(1),
    '&::placeholder': {
      color: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
      opacity: 1,
    },
  },
  '& .MuiChip-root': {
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    ...theme.typography.caption,
    '& .MuiChip-deleteIcon': {
      color: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
    },
  },
}));
