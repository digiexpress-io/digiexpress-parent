import { generateUtilityClass, styled, alpha } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';
import { OwnerState } from './useOwnerState';

const MUI_NAME = 'FsDirentButtonCancel';

export interface FsDirentButtonCancelClasses {
  root: string;
}

export type FsDirentButtonCancelClassKey = keyof FsDirentButtonCancelClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentButtonCancelRoot = styled('button', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ ownerState, theme }) => ({
  
  backgroundColor: alpha(ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight, 0.1),
  color: ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight,
  border: `1px solid ${alpha(ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight, 0.3)}`,
  borderRadius: theme.spacing(0.5),
  minWidth: '13ch',
  padding: theme.spacing(0.5),
  ...theme.typography.subtitle2,
  cursor: 'pointer',
  '&:hover': {
    backgroundColor: alpha(ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight, 0.2),
  },
}));
