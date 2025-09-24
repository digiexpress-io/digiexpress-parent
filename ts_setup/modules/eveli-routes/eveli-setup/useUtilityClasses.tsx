import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

export const MUI_NAME = 'ToolbarBuildInfo';

export interface ToolbarBuildInfoClasses {
  root: string;
}

export type ToolbarBuildInfoClassKey = keyof ToolbarBuildInfoClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const ToolbarBuildInfoRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
})(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  padding: 0,
  gap: theme.spacing(1),
}));
