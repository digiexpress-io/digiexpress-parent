import { generateUtilityClass, styled, darken, lighten } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';

const MUI_NAME = 'FsDirentArticlePage';

export interface FsDirentArticlePageClasses {
  root: string;
  title: string;
  formContainer: string;
  label: string;
}

export type FsDirentArticlePageClassKey = keyof FsDirentArticlePageClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    formContainer: ['formContainer'],
    label: ['label'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentArticlePageRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: { isDarkMode: boolean } }>(({ theme, ownerState }) => ({
  display: 'flex',
  flexDirection: 'column',

  [`& .${MUI_NAME}-title`]: {
    ...theme.typography.body1,
    fontWeight: 500,
    marginBottom: theme.spacing(2),
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
  },

  [`& .${MUI_NAME}-formContainer`]: {
    display: 'flex',
    flexDirection: 'column',
    gap: theme.spacing(1.5),
    padding: '8px 0',
  },

  [`& .${MUI_NAME}-label`]: {
    ...theme.typography.subtitle2,
    fontWeight: 500,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
  },


}));
