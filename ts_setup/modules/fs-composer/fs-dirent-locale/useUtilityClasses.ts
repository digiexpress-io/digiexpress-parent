import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';

const MUI_NAME = 'FsDirentLocale';

export interface FsDirentLocaleClasses {
  root: string;
  titleRow: string;
  title: string;
  formContainer: string;
}

export type FsDirentLocaleClassKey = keyof FsDirentLocaleClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    titleRow: ['titleRow'],
    title: ['title'],
    formContainer: ['formContainer'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentLocaleRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',

  [`& .${MUI_NAME}-titleRow`]: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: theme.spacing(2),
  },

  [`& .${MUI_NAME}-title`]: {
    ...theme.typography.body1,
    fontWeight: 500,
    color: FsColors.base.text,
    marginBottom: theme.spacing(2)
  },

  [`& .${MUI_NAME}-formContainer`]: {
    display: 'flex',
    flexDirection: 'column',
  },


}));
