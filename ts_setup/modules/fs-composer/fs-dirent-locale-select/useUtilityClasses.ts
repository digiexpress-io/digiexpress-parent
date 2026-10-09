import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';

const MUI_NAME = 'FsDirentLocaleSelect';

export interface FsDirentLocaleSelectClasses {
  root: string;
}

export type FsDirentLocaleSelectClassKey = keyof FsDirentLocaleSelectClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentLocaleSelectRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})(({ theme }) => ({
  marginTop: '0px !important',

  '& .MuiFormControl-root': {
    width: '25%',
    marginTop: '0 !important',
    marginBottom: '0 !important',
  },
  '& .MuiOutlinedInput-root': {
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
    ...theme.typography.subtitle1,
    padding: theme.spacing(1),
    '&::placeholder': {
      color: FsColors.base.textSecondary,
      opacity: 1,
    },
  },
}));

export const FsDirentLocaleSelectReadOnly = styled('div', {
  name: MUI_NAME,
  slot: 'ReadOnly',
})(({ theme }) => ({
  backgroundColor: FsColors.base.background,
  color: FsColors.base.text,
  borderRadius: 0,
  width: '25%',
  display: 'flex',
  alignItems: 'center',
  border: `1px solid ${FsColors.base.border}`,
  padding: theme.spacing(1),
  ...theme.typography.subtitle1,
}));
