import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';

const MUI_NAME = 'FsDirentPrintout';

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    titleRow: ['titleRow'],
    title: ['title'],
    formContainer: ['formContainer'],
    pageList: ['pageList'],
    localeRow: ['localeRow'],
    localeSelectContainer: ['localeSelectContainer'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentPrintoutRoot = styled('div', {
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
    color: FsColors.base.text,
    marginBottom: theme.spacing(2),
    fontWeight: 500,
  },

  [`& .${MUI_NAME}-formContainer`]: {
    display: 'flex',
    flexDirection: 'column',
  },

  [`& .${MUI_NAME}-pageList`]: {
    display: 'flex',
    flexDirection: 'column',
    gap: theme.spacing(0.5),
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