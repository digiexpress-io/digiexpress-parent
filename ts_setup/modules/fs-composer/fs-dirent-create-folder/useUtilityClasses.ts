import { generateUtilityClass, styled, alpha } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';
import { OwnerState } from './useOwnerState';

const MUI_NAME = 'FsDirentCreateFolder';

export interface FsDirentCreateFolderClasses {
  root: string;
  title: string;
  formContainer: string;
  label: string;
  textField: string;
  buttonContainer: string;
  cancelButton: string;
  saveButton: string;
}

export type FsDirentCreateFolderClassKey = keyof FsDirentCreateFolderClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    formContainer: ['formContainer'],
    label: ['label'],
    textField: ['textField'],
    buttonContainer: ['buttonContainer'],
    cancelButton: ['cancelButton'],
    saveButton: ['saveButton'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentCreateFolderRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => ({
  display: 'flex',
  flexDirection: 'column',

  [`& .${MUI_NAME}-title`]: {
    ...theme.typography.body1,
    fontWeight: 500,
    marginBottom: theme.spacing(2),
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
  },

  [`& .${MUI_NAME}-formContainer`]: {
    display: 'flex',
    flexDirection: 'column',
    gap: theme.spacing(1.5),
    padding: '8px 0',
  },

  [`& .${MUI_NAME}-label`]: {
    ...theme.typography.subtitle2,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
  },

  [`& .${MUI_NAME}-textField`]: {
    width: '100%',
    marginTop: '0px !important',
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
        border: `1px solid ${ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text}`
      },
    },
    '& .MuiInputBase-input': {
      color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
      ...theme.typography.caption,
      padding: '8px 12px',
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
  },

  [`& .${MUI_NAME}-buttonContainer`]: {
    display: 'flex',
    gap: '12px',
    marginTop: '16px',
    justifyContent: 'flex-end',
  },

  [`& .${MUI_NAME}-cancelButton`]: {
    backgroundColor: alpha(ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight, 0.1),
    color: ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight,
    border: `1px solid ${alpha(ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight, 0.3)}`,
    borderRadius: '4px',
    padding: '6px 16px',
    textTransform: 'none',
    fontSize: '12px',
    cursor: 'pointer',
    '&:hover': {
      backgroundColor: alpha(ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight, 0.2),
    },
  },

  [`& .${MUI_NAME}-saveButton`]: {
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border,
    color: ownerState.isDarkMode ? FsColors.light.background : FsColors.light.text,
    border: `1px solid ${ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.dark.textSecondary}`,
    borderRadius: '4px',
    padding: '6px 16px',
    textTransform: 'none',
    fontSize: '12px',
    fontWeight: 500,
    cursor: 'pointer',
    '&:hover': {
      backgroundColor: ownerState.isDarkMode ? FsColors.dark.border : FsColors.dark.text,
      borderColor: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.dark.textSecondary,
    },
  },
}));
