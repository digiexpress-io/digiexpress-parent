import { generateUtilityClass, styled, FormControl } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../../fs-theme';

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
})(({ theme }) => ({
  width: '100%',
  marginTop: 'unset !important',

  [`& .${MUI_NAME}-select`]: {
    backgroundColor: FsColors.base.background,
    color: FsColors.base.text,
    borderRadius: 0,
    '& .MuiOutlinedInput-notchedOutline': {
      borderColor: FsColors.base.border,
      borderRadius: 0,
    },
    '&:hover .MuiOutlinedInput-notchedOutline': {
      borderColor: FsColors.base.textSecondary,
    },
    '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
      border: `1px solid ${FsColors.base.text}`,
    },
    '& .MuiSvgIcon-root': {
      color: FsColors.base.text,
    },
    '& .MuiSelect-select': {
      backgroundColor: FsColors.base.background,
      color: FsColors.base.text,
      padding: theme.spacing(1.5),
      ...theme.typography.subtitle1,
    },
  },

  [`& .${MUI_NAME}-placeholderText`]: {
    ...theme.typography.subtitle1,
    color: FsColors.base.textSecondary,
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
    color: FsColors.base.textSecondary,
    '&:hover': {
      backgroundColor: 'transparent',
      color: FsColors.base.text,
    },
  },

  [`& .${MUI_NAME}-chip`]: {
    backgroundColor: FsColors.base.border,
    color: FsColors.base.text,
    '&.MuiChip-root': {
      backgroundColor: FsColors.base.border,
      color: FsColors.base.text,
    },
    '& .MuiChip-label': {
      color: FsColors.base.text,
      ...theme.typography.caption,
    },
    '& .MuiChip-deleteIcon': {
      color: FsColors.base.textSecondary,
      '&:hover': {
        color: FsColors.base.text,
      },
    },
  },
}));
