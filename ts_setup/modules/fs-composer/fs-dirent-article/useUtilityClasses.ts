import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';

const MUI_NAME = 'FsDirentArticle';

export interface FsDirentArticleClasses {
  root: string;
  titleRow: string;
  title: string;
  formContainer: string;
  sectionTitle: string;
  sectionBox: string;
  sectionContent: string;
}

export type FsDirentArticleClassKey = keyof FsDirentArticleClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    titleRow: ['titleRow'],
    title: ['title'],
    formContainer: ['formContainer'],
    sectionTitle: ['sectionTitle'],
    sectionBox: ['sectionBox'],
    sectionContent: ['sectionContent'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentArticleRoot = styled('div', {
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
    color: FsColors.light.text,
    marginBottom: theme.spacing(2)
  },

  [`& .${MUI_NAME}-formContainer`]: {
    display: 'flex',
    flexDirection: 'column',
  },

  [`& .${MUI_NAME}-sectionTitle`]: {
    ...theme.typography.subtitle2,
    color: FsColors.light.text,
    display: 'block',
  },

  [`& .${MUI_NAME}-sectionBox`]: {
    backgroundColor: FsColors.light.border,
    border: `1px solid ${FsColors.dark.textSecondary}`,
    borderRadius: '4px',
    marginTop: '0px !important'
  },

  [`& .${MUI_NAME}-sectionContent`]: {
    ...theme.typography.subtitle2,
    color: FsColors.light.textSecondary,
  },

}));
