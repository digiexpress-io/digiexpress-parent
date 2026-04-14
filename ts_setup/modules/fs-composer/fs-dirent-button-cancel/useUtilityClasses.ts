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
  
  backgroundColor: 'transparent',
  color: ownerState.isDarkMode ? FsColors.light.background : FsColors.light.text,
  border: `1px solid ${ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.dark.textSecondary}`,
  borderRadius: theme.spacing(0.5),
  minWidth: '13ch',
  padding: theme.spacing(0.5),
  ...theme.typography.subtitle2,
  cursor: 'pointer',
  '&:hover': {
    backgroundColor: alpha(ownerState.isDarkMode ? FsColors.light.background : FsColors.light.text, 0.05),
  },
}));
