import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';

const MUI_NAME = 'FsDirentFolder';

export interface FsDirentFolderClasses {
  root: string;
  title: string;
  formContainer: string;
}

export type FsDirentFolderClassKey = keyof FsDirentFolderClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    formContainer: ['formContainer'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentFolderRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',

  [`& .${MUI_NAME}-title`]: {
    ...theme.typography.body1,
    fontWeight: 500,
    marginBottom: theme.spacing(2),
    color: FsColors.light.text,
  },

  [`& .${MUI_NAME}-formContainer`]: {
    display: 'flex',
    flexDirection: 'column',
  },


}));
