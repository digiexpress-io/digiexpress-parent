import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';
import { OwnerState } from './useOwnerState';

const MUI_NAME = 'FsDirentButtonCreate';

export interface FsDirentButtonCreateClasses {
  root: string;
}

export type FsDirentButtonCreateClassKey = keyof FsDirentButtonCreateClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentButtonCreateRoot = styled('button', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ ownerState, theme }) => ({
  backgroundColor: ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border,
  color: ownerState.isDarkMode ? FsColors.light.background : FsColors.light.text,
  border: `1px solid ${ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.dark.textSecondary}`,
  borderRadius: theme.spacing(0.5),
  minWidth: '13ch',
  padding: theme.spacing(0.5),
  ...theme.typography.subtitle2,
  cursor: 'pointer',  
  '&:hover': {
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.border : FsColors.dark.text,
    borderColor: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.dark.textSecondary,
  },
}));
