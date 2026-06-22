import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';

const MUI_NAME = 'FsDiffIndicator';

export interface FsDiffIndicatorClasses {
  root: string;
}

export type FsDiffIndicatorClassKey = keyof FsDiffIndicatorClasses;

export const useUtilityClasses = () => {
  const slots = { root: ['root'] };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDiffIndicatorRoot = styled('span', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})(() => ({
  display: 'inline-flex',
  alignItems: 'center',
  color: FsColors.semantic.warning,
}));
