import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';

const MUI_NAME = 'FsDirentRename';

export interface FsDirentRenameClasses {
  root: string;
  title: string;
  titleRow: string;
  textField: string;
}

export type FsDirentRenameClassKey = keyof FsDirentRenameClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    titleRow: ['titleRow'],
    textField: ['textField'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentRenameRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',

  [`& .${MUI_NAME}-titleRow`]: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: theme.spacing(1),
  },

  [`& .${MUI_NAME}-title`]: {
    ...theme.typography.subtitle2,
    fontWeight: 500,
    color: FsColors.base.text,
  },

  [`& .${MUI_NAME}-textField`]: {
    width: '100%',
    marginTop: theme.spacing(0.5),
    '& .MuiInputBase-root': {
      backgroundColor: FsColors.base.background,
      color: FsColors.base.text,
      borderRadius: 0,
      '& fieldset': {
        borderColor: FsColors.base.border,
        borderRadius: 0,
      },
      '&:hover fieldset': {
        borderColor: FsColors.base.textSecondary,
      },
      '&.Mui-focused fieldset': {
        border: `1px solid ${FsColors.base.text}`,
      },
    },
    '& .MuiInputBase-input': {
      color: FsColors.base.text,
      ...theme.typography.caption,
      padding: '8px 12px',
      '&::placeholder': {
        color: FsColors.base.textSecondary,
        opacity: 1,
        ...theme.typography.caption,
      },
    },
    '& .MuiInputLabel-root': {
      color: FsColors.base.text,
      ...theme.typography.caption,
    },
  },
}));
