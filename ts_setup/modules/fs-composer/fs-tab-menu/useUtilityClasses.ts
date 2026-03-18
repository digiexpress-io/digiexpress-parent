import composeClasses from '@mui/utils/composeClasses';
import { generateUtilityClass, styled, Popover } from '@mui/material';
import { FsColors } from '../fs-theme';

export const MUI_NAME = 'FsTabMenu';

export interface FsTabMenuClasses {
  root: string;
  menuItem: string;
}

export type FsTabMenuClassKey = keyof FsTabMenuClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    menuItem: ['menuItem'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsTabMenuRoot = styled(Popover, {
  name: MUI_NAME,
  slot: 'Root',
})(({ theme }) => ({
  '& .MuiPaper-root': {
    backgroundColor: FsColors.dark.surface,
    color: FsColors.dark.text,
    border: `1px solid ${FsColors.dark.border}`,
    padding: theme.spacing(0.5),
  },
  [`& .${MUI_NAME}-menuItem`]: {
    fontSize: '13px',
    display: 'flex',
    alignItems: 'center',
    gap: theme.spacing(1),
    '&:hover': {
      backgroundColor: FsColors.dark.border,
    },
    '& .MuiSvgIcon-root': {
      color: FsColors.dark.text,
      fontSize: '16px',
    },
  },
}));
