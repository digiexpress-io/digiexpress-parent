import { generateUtilityClass, styled, alpha } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';

const MUI_NAME = 'FsDirentButtonDelete';

export interface FsDirentButtonDeleteClasses {
  root: string;
}

export type FsDirentButtonDeleteClassKey = keyof FsDirentButtonDeleteClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentButtonDeleteRoot = styled('button', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})(({ theme }) => ({
  backgroundColor: alpha(FsColors.semantic.danger, 0.1),
  color: FsColors.semantic.danger,
  border: `1px solid ${alpha(FsColors.semantic.danger, 0.3)}`,
  borderRadius: theme.spacing(0.5),
  minWidth: '13ch',
  padding: theme.spacing(0.5),
  ...theme.typography.subtitle2,
  cursor: 'pointer',
  '&:hover': {
    backgroundColor: alpha(FsColors.semantic.danger, 0.2),
  },
}));
