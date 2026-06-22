import { generateUtilityClass, styled, alpha, darken, Button } from '@mui/material';
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

export const FsDirentButtonSaveAllRoot = styled(Button, {
  name: MUI_NAME,
  slot: 'AllRoot',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ ownerState, theme }) => ({
  backgroundColor: alpha(FsColors.semantic.success, 0.25),
  color: darken(FsColors.semantic.success, 0.7),
  border: `1px solid ${alpha(FsColors.semantic.success, 0.7)}`,
  borderRadius: theme.spacing(0.5),
  padding: theme.spacing(0.5),
  minWidth: '13ch',
  ...theme.typography.subtitle2,
  cursor: 'pointer',
  textTransform: 'none',
  boxShadow: 'none',
  '&.Mui-disabled': {
    opacity: 0.4,
    color: darken(FsColors.semantic.success, 0.7),
    backgroundColor: alpha(FsColors.semantic.success, 0.25),
    border: `1px solid ${alpha(FsColors.semantic.success, 0.7)}`,
  },
  '&:hover': {
    boxShadow: 'none',
    backgroundColor: alpha(FsColors.semantic.success, 0.5),
  },
}));

export const FsDirentButtonSaveRoot = styled(Button, {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ ownerState, theme }) => ({
  backgroundColor: alpha(FsColors.semantic.success, 0.1),
  color: FsColors.semantic.success,
  border: `1px solid ${alpha(FsColors.semantic.success, 0.3)}`,
  borderRadius: theme.spacing(0.5),
  minWidth: '13ch',
  padding: theme.spacing(0.5),
  ...theme.typography.subtitle2,
  textTransform: 'none',
  boxShadow: 'none',
  cursor: ownerState.disabled ? 'not-allowed' : 'pointer',
  opacity: ownerState.disabled ? 0.4 : 1,
  '&:hover': {
    boxShadow: 'none',
    backgroundColor: ownerState.disabled ? alpha(FsColors.semantic.success, 0.1) : alpha(FsColors.semantic.success, 0.2),
  },
}));

export const FsDirentButtonOpenRoot = styled(Button, {
  name: MUI_NAME,
  slot: 'OpenRoot',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ ownerState, theme }) => ({
  backgroundColor: alpha(FsColors.direntTypes.form, 0.1),
  color: FsColors.direntTypes.form,
  border: `1px solid ${alpha(FsColors.direntTypes.form, 0.3)}`,
  borderRadius: theme.spacing(0.5),
  minWidth: '13ch',
  alignSelf: 'flex-start',
  padding: theme.spacing(0.5),
  ...theme.typography.subtitle2,
  textTransform: 'none',
  boxShadow: 'none',
  cursor: ownerState.disabled ? 'not-allowed' : 'pointer',
  opacity: ownerState.disabled ? 0.4 : 1,
  '&:hover': {
    boxShadow: 'none',
    backgroundColor: ownerState.disabled ? alpha(FsColors.direntTypes.form, 0.1) : alpha(FsColors.direntTypes.form, 0.2),
  },
}));
