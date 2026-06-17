import { generateUtilityClass, styled, FormControl } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../../fs-theme';
import { OwnerState } from './useOwnerState';

const MUI_NAME = 'FsDirentSelectSingle';

export interface FsDirentSelectSingleClasses {
  root: string;
  select: string;
}

export type FsDirentSelectSingleClassKey = keyof FsDirentSelectSingleClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    select: ['select'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentSelectSingleRoot = styled(FormControl, {
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
}));
