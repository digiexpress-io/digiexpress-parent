import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';

const MUI_NAME = 'FsDirentPrintoutPage';

export interface FsDirentPrintoutPageClasses {
  root: string;
  titleRow: string;
  title: string;
  formContainer: string;
  resourceList: string;
  dialogListItem: string;
  dialogItemEnd: string;
  dialogCheckmark: string;
  dialogThumbnail: string;
}

export type FsDirentPrintoutPageClassKey = keyof FsDirentPrintoutPageClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    titleRow: ['titleRow'],
    title: ['title'],
    formContainer: ['formContainer'],
    resourceList: ['resourceList'],
    dialogListItem: ['dialogListItem'],
    dialogItemEnd: ['dialogItemEnd'],
    dialogCheckmark: ['dialogCheckmark'],
    dialogThumbnail: ['dialogThumbnail'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentPrintoutPageRoot = styled('div', {
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
    fontWeight: 500
  },

  [`& .${MUI_NAME}-formContainer`]: {
    display: 'flex',
    flexDirection: 'column',
  },

  [`& .${MUI_NAME}-resourceList`]: {
    display: 'flex',
    flexDirection: 'column',
    gap: theme.spacing(0.5),
  },

}));

export const FsDirentPrintoutPageDialogList = styled('div', {
  name: MUI_NAME,
  slot: 'DialogList',
})(({ theme }) => ({
  [`& .${MUI_NAME}-dialogListItem`]: {
    '& .MuiListItemText-primary': {
      fontWeight: 500,
    },
  },

  [`& .${MUI_NAME}-dialogItemEnd`]: {
    display: 'flex',
    alignItems: 'center',
    gap: theme.spacing(0.5),
    marginLeft: theme.spacing(1),
    flexShrink: 0,
  },

  [`& .${MUI_NAME}-dialogCheckmark`]: {
    fontSize: '20px !important',
    color: theme.palette.success.main,
    flexShrink: 0,
  },

  [`& .${MUI_NAME}-dialogThumbnail`]: {
    width: 48,
    height: 48,
    objectFit: 'cover' as const,
    flexShrink: 0,
  },
}));
