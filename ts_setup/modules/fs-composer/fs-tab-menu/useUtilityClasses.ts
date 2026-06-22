import composeClasses from '@mui/utils/composeClasses';
import { generateUtilityClass, styled, Popover, darken } from '@mui/material';
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
})(({ theme }) => {

  const borderColor = darken(FsColors.base.border, 0.15);

  return {
    '& .MuiPaper-root': {
      backgroundColor: darken(FsColors.base.surface, 0.06),
      color: FsColors.base.text,
      border: `1px solid ${borderColor}`,
      padding: theme.spacing(0.5),
    },
    [`& .${MUI_NAME}-menuItem`]: {
      fontSize: '13px',
      display: 'flex',
      alignItems: 'center',
      gap: theme.spacing(1),
      '&:hover': {
        backgroundColor: FsColors.base.border,
      },
      '& .MuiSvgIcon-root': {
        color: FsColors.base.text,
        fontSize: '16px',
      },
    },
  };
});
