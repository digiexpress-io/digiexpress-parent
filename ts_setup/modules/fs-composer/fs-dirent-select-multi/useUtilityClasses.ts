import { generateUtilityClass, styled, FormControl } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';
import { OwnerState } from './useOwnerState';

const MUI_NAME = 'FsDirentSelectMulti';

export interface FsDirentSelectMultiClasses {
  root: string;
  select: string;
  chipContainer: string;
  chip: string;
  menuItem: string;
  placeholderText: string;
  clearButton: string;
}

export type FsDirentSelectMultiClassKey = keyof FsDirentSelectMultiClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    select: ['select'],
    chipContainer: ['chipContainer'],
    chip: ['chip'],
    menuItem: ['menuItem'],
    placeholderText: ['placeholderText'],
    clearButton: ['clearButton'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentSelectMultiRoot = styled(FormControl, {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => ({
  width: '100%',
  marginTop: 'unset !important',

  [`& .${MUI_NAME}-select`]: {
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.background : FsColors.light.background,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    borderRadius: 0,
    '& .MuiOutlinedInput-notchedOutline': {
      borderColor: ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border,
      borderRadius: 0,
    },
    '&:hover .MuiOutlinedInput-notchedOutline': {
      borderColor: ownerState.isDarkMode ? FsColors.light.textSecondary : FsColors.dark.textSecondary,
    },
    '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
      border: `1px solid ${ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text}`,
    },
    '& .MuiSvgIcon-root': {
      color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    },
    '& .MuiSelect-select': {
      backgroundColor: ownerState.isDarkMode ? FsColors.dark.background : FsColors.light.background,
      color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
      padding: theme.spacing(1.5),
      ...theme.typography.caption,
    },
  },

  [`& .${MUI_NAME}-placeholderText`]: {
    ...theme.typography.caption,
    color: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
  },

  [`& .${MUI_NAME}-chipContainer`]: {
    display: 'flex',
    flexWrap: 'wrap',
    alignItems: 'center',
    minHeight: theme.spacing(3),
    gap: theme.spacing(0.5),
    maxWidth: `calc(100% - ${theme.spacing(4)})`,
  },

  [`& .${MUI_NAME}-clearButton`]: {
    position: 'absolute',
    right: theme.spacing(4),
    top: '50%',
    transform: 'translateY(-50%)',
    padding: 0,
    color: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
    '&:hover': {
      backgroundColor: 'transparent',
      color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    },
  },

  [`& .${MUI_NAME}-chip`]: {
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    '&.MuiChip-root': {
      backgroundColor: ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border,
      color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    },
    '& .MuiChip-label': {
      color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
      ...theme.typography.caption,
    },
    '& .MuiChip-deleteIcon': {
      color: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
      '&:hover': {
        color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
      },
    },
  },
}));
