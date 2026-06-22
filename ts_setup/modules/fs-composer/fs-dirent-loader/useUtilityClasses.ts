import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

const MUI_NAME = 'FsDirentLoader';

export interface FsDirentLoaderClasses {
  root: string;
}

export type FsDirentLoaderClassKey = keyof FsDirentLoaderClasses;

export const useUtilityClasses = () => {
  const slots = { root: ['root'] };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentLoaderRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})(() => ({
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  width: '100%',
  height: '100%',
}));
