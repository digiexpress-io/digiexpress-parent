import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';

const MUI_NAME = 'FsDirentMenuNew';

export interface FsDirentMenuNewClasses {
  root: string;
  title: string;
  listItem: string;
}

export type FsDirentMenuNewClassKey = keyof FsDirentMenuNewClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    listItem: ['listItem'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentMenuNewRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',

  [`& .${MUI_NAME}-title`]: {
    ...theme.typography.subtitle2,
    fontWeight: 500,
    color: FsColors.base.text,
    marginBottom: theme.spacing(1),
  },

  [`& .${MUI_NAME}-listItem`]: {
    ...theme.typography.subtitle2,
    display: 'flex',
    alignItems: 'center',
    gap: theme.spacing(1),
    color: FsColors.base.text,
    padding: theme.spacing(0.75, 1),
    cursor: 'pointer',
    '&:hover': {
      backgroundColor: FsColors.base.border,
    },
  },
}));
