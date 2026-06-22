import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';

const MUI_NAME = 'FsDirentArticleWorkflowRoot';

export interface FsDirentArticleWorkflowClasses {
  root: string;
  titleRow: string;
  title: string;
  formContainer: string;
  localeLabel: string;
  localeRow: string;
  sectionTitle: string;
}

export type FsDirentArticleWorkflowClassKey = keyof FsDirentArticleWorkflowClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    titleRow: ['titleRow'],
    title: ['title'],
    formContainer: ['formContainer'],
    localeLabel: ['localeLabel'],
    localeRow: ['localeRow'],
    sectionTitle: ['sectionTitle'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentArticleWorkflowRoot = styled('div', {
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
    marginBottom: theme.spacing(2),
    color: FsColors.light.text,
    '&.MuiTypography-root': {
      color: FsColors.light.text,
    },
  },

  [`& .${MUI_NAME}-formContainer`]: {
    display: 'flex',
    flexDirection: 'column',
  },

  [`& .${MUI_NAME}-localeLabel`]: {
    ...theme.typography.subtitle2,
    fontWeight: 500,
    color: FsColors.light.text,
    '&.MuiTypography-root': {
      color: FsColors.light.text,
    },
  },

  [`& .${MUI_NAME}-localeRow`]: {
    display: 'flex',
    flexDirection: 'column',
    gap: theme.spacing(0.5),
  },

  [`& .${MUI_NAME}-sectionTitle`]: {
    ...theme.typography.subtitle2,
    color: FsColors.light.text,
    display: 'block',
    '&.MuiTypography-root': {
      color: FsColors.light.text,
    },
  },

}));
