import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';
import { OwnerState } from './useOwnerState';

const MUI_NAME = 'FsDirentRename';

export interface FsDirentRenameClasses {
  root: string;
  title: string;
  textField: string;
}

export type FsDirentRenameClassKey = keyof FsDirentRenameClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    textField: ['textField'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentRenameRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => ({
  display: 'flex',
  flexDirection: 'column',

  [`& .${MUI_NAME}-title`]: {
    ...theme.typography.caption,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    marginBottom: theme.spacing(1),
  },

  [`& .${MUI_NAME}-textField`]: {
    width: '100%',
    marginTop: theme.spacing(0.5),
    '& .MuiInputBase-root': {
      backgroundColor: ownerState.isDarkMode ? FsColors.dark.background : FsColors.light.background,
      color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
      borderRadius: 0,
      '& fieldset': {
        borderColor: ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border,
        borderRadius: 0,
      },
      '&:hover fieldset': {
        borderColor: FsColors.light.textSecondary,
      },
      '&.Mui-focused fieldset': {
        border: `1px solid ${ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text}`,
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
    '& .MuiInputLabel-root': {
      color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
      ...theme.typography.caption,
    },
  },
}));
