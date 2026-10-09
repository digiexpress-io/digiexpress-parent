import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';

const MUI_NAME = 'FsDirentArticleLink';

export interface FsDirentArticleLinkClasses {
  root: string;
  titleRow: string;
  title: string;
  formContainer: string;
  localeRow: string;
  localeSelectContainer: string;
}

export type FsDirentArticleLinkClassKey = keyof FsDirentArticleLinkClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    titleRow: ['titleRow'],
    title: ['title'],
    formContainer: ['formContainer'],
    localeRow: ['localeRow'],
    localeSelectContainer: ['localeSelectContainer'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentArticleLinkRoot = styled('div', {
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

  [`& .${MUI_NAME}-localeRow`]: {
    display: 'flex',
    flexDirection: 'row',
    gap: theme.spacing(1),
    alignItems: 'flex-start',
  },

  [`& .${MUI_NAME}-localeSelectContainer`]: {
    width: '25%',
    flexShrink: 0,
    '& .MuiFormControl-root': {
      width: '100%',
      marginTop: '0 !important',
      marginBottom: '0 !important',
    },
  },

  [`& .${MUI_NAME}-localeRow + .${MUI_NAME}-localeRow`]: {
    marginTop: theme.spacing(1),
  },


}));
