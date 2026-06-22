import composeClasses from '@mui/utils/composeClasses';
import { generateUtilityClass, styled } from '@mui/material';
import { FsColors } from '../fs-theme';
import { OwnerState } from './useOwnerState';
import { FsSearchProps } from './FsSearchProps';

export const MUI_NAME = 'FsSearch';

export interface FsSearchClasses {
  root: string;
  container: string;
  searchField: string;
  multiSelect: string;
  chipContainer: string;
  placeholderText: string;
}

export type FsSearchClassKey = keyof FsSearchClasses;

export const useUtilityClasses = (_props: FsSearchProps) => {
  const slots = {
    root: ['root'],
    container: ['container'],
    searchField: ['searchField'],
    multiSelect: ['multiSelect'],
    chipContainer: ['chipContainer'],
    placeholderText: ['placeholderText'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsSearchRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => ({
  display: 'flex',
  flexDirection: 'column',

  [`& .${MUI_NAME}-container`]: {
    display: 'flex',
    padding: theme.spacing(1),
    flexDirection: 'column',
    gap: theme.spacing(1),
    backgroundColor: FsColors.light.surface,
  },

  [`& .${MUI_NAME}-searchField`]: {
    marginTop: 0,

    '& .MuiInputBase-root': {
      borderRadius: 0,
      backgroundColor: theme.palette.background.paper,
      color: FsColors.light.text,
    },

    '& .MuiInputBase-input': {
      padding: theme.spacing(1),
      ...theme.typography.subtitle2,
      color: FsColors.light.text,
    },

    '& fieldset': {
      borderColor: FsColors.light.border,
      borderRadius: 0,
    },

    '&:hover fieldset': {
      borderColor: FsColors.light.text,
    },

    '&.Mui-focused fieldset': {
      border: `1px solid ${theme.palette.primary.main}`,
    },

    '& .MuiInputBase-input::placeholder': {
      color: theme.palette.text.secondary,
      opacity: 0.7,
    },
  },

  [`& .${MUI_NAME}-multiSelect`]: {
    marginTop: 0,
    borderRadius: 0,

    '& .MuiInputBase-input': {
      padding: theme.spacing(1),
      ...theme.typography.subtitle2,
      color: FsColors.light.text,
    },

    '& .MuiOutlinedInput-root': {
      backgroundColor: theme.palette.background.paper,
    },

    '& .MuiOutlinedInput-notchedOutline': {
      borderColor: FsColors.light.border,
    },

    '&:hover .MuiOutlinedInput-notchedOutline': {
      borderColor: FsColors.light.text,
    },

    '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
      borderColor: theme.palette.primary.main,
      borderWidth: '1px',
    },

    '& .MuiSvgIcon-root': {
      color: FsColors.light.text,
    },
  },

  [`& .${MUI_NAME}-chipContainer`]: {
    display: 'flex',
    flexWrap: 'wrap',
    gap: theme.spacing(0.5),
  },

  [`& .${MUI_NAME}-placeholderText`]: {
    ...theme.typography.subtitle2,
    color: theme.palette.text.primary,
  },

  '& .MuiDivider-root': {
    borderColor: FsColors.light.border,
  },
}));