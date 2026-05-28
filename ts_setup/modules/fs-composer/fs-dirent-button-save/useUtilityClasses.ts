import { generateUtilityClass, styled, alpha } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';
import { OwnerState } from './useOwnerState';

const MUI_NAME = 'FsDirentButtonSave';

export interface FsDirentButtonSaveClasses {
  root: string;
}

export type FsDirentButtonSaveClassKey = keyof FsDirentButtonSaveClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentButtonSaveRoot = styled('button', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ ownerState, theme }) => ({
  backgroundColor: alpha(ownerState.isDarkMode ? FsColors.direntTypes.dark.link : FsColors.semantic.success, 0.1),
  color: ownerState.isDarkMode ? FsColors.direntTypes.dark.link : FsColors.semantic.success,
  border: `1px solid ${alpha(ownerState.isDarkMode ? FsColors.direntTypes.dark.link : FsColors.semantic.success, 0.3)}`,
  borderRadius: theme.spacing(0.5),
  minWidth: '13ch',
  padding: theme.spacing(0.5),
  ...theme.typography.subtitle2,
  cursor: ownerState.disabled ? 'not-allowed' : 'pointer',
  opacity: ownerState.disabled ? 0.4 : 1,
  '&:hover': {
    backgroundColor: ownerState.disabled
      ? alpha(ownerState.isDarkMode ? FsColors.direntTypes.dark.link : FsColors.semantic.success, 0.1)
      : alpha(ownerState.isDarkMode ? FsColors.direntTypes.dark.link : FsColors.semantic.success, 0.2),
  },
}));
