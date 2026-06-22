import { generateUtilityClass, styled, alpha, Button } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';

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

export const FsDirentButtonCancelAllRoot = styled(Button, {
  name: MUI_NAME,
  slot: 'AllRoot',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})(({ theme }) => ({
  backgroundColor: alpha(FsColors.semantic.dangerLight, 0.1),
  color: FsColors.semantic.dangerLight,
  border: `1px solid ${alpha(FsColors.semantic.dangerLight, 0.5)}`,
  borderRadius: theme.spacing(0.5),
  minWidth: '13ch',
  padding: theme.spacing(0.5),
  ...theme.typography.subtitle2,
  textTransform: 'none',
  boxShadow: 'none',
  cursor: 'pointer',
  '&.Mui-disabled': {
    opacity: 0.4,
    color: FsColors.semantic.dangerLight,
    backgroundColor: alpha(FsColors.semantic.dangerLight, 0.1),
    border: `1px solid ${alpha(FsColors.semantic.dangerLight, 0.5)}`,
  },
  '&:hover': {
    boxShadow: 'none',
    backgroundColor: alpha(FsColors.semantic.dangerLight, 0.2),
  },
}));

export const FsDirentButtonCancelRoot = styled(Button, {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})(({ theme }) => ({
  backgroundColor: 'transparent',
  color: FsColors.light.text,
  border: `1px solid ${FsColors.dark.textSecondary}`,
  borderRadius: theme.spacing(0.5),
  minWidth: '13ch',
  padding: theme.spacing(0.5),
  ...theme.typography.subtitle2,
  textTransform: 'none',
  boxShadow: 'none',
  cursor: 'pointer',
  '&:hover': {
    boxShadow: 'none',
    backgroundColor: alpha(FsColors.light.text, 0.05),
  },
}));
