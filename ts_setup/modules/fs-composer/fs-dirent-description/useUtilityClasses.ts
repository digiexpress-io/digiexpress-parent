import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';

const MUI_NAME = 'FsDirentDescription';

export interface FsDirentDescriptionClasses {
  root: string;
  textField: string;
  title: string;
  titleRow: string;
}

export type FsDirentDescriptionClassKey = keyof FsDirentDescriptionClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    textField: ['textField'],
    title: ['title'],
    titleRow: ['titleRow'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentDescriptionRoot = styled('div', {
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
    color: FsColors.light.text,
    marginBottom: theme.spacing(2)
  },

  [`& .${MUI_NAME}-textField`]: {
    width: '100%',
    marginTop: theme.spacing(0.5),
    '& .MuiInputBase-root': {
      backgroundColor: FsColors.light.background,
      color: FsColors.light.text,
      borderRadius: 0,
      '& fieldset': {
        borderColor: FsColors.light.border,
        borderRadius: 0,
      },
      '&:hover fieldset': {
        borderColor: FsColors.light.textSecondary,
      },
      '&.Mui-focused fieldset': {
        border: `1px solid ${FsColors.light.text}`,
      },
    },
    '& .MuiInputBase-input': {
      color: FsColors.light.text,
      ...theme.typography.caption,
      padding: '8px 12px',
      '&::placeholder': {
        color: FsColors.light.textSecondary,
        opacity: 1,
        ...theme.typography.caption,
      },
    },
    '& .MuiInputBase-input.MuiOutlinedInput-input.MuiInputBase-inputMultiline': {
      padding: 'unset',
    },
    '& .MuiInputLabel-root': {
      color: FsColors.light.text,
      ...theme.typography.caption,
    },
  },
}));
