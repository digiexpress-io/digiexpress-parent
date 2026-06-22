import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';

const MUI_NAME = 'FsDirentComments';

export interface FsDirentCommentsClasses {
  root: string;
  title: string;
  textField: string;
  spacer: string;
  commentsContainer: string;
  noComments: string;
  commentItem: string;
  divider: string;
  commentContent: string;
  commentMeta: string;
}

export type FsDirentCommentsClassKey = keyof FsDirentCommentsClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    textField: ['textField'],
    spacer: ['spacer'],
    commentsContainer: ['commentsContainer'],
    noComments: ['noComments'],
    commentItem: ['commentItem'],
    divider: ['divider'],
    commentContent: ['commentContent'],
    commentMeta: ['commentMeta'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentCommentsRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',

  [`& .${MUI_NAME}-title`]: {
    ...theme.typography.caption,
    fontWeight: 500,
    color: FsColors.base.text,
  },

  [`& .${MUI_NAME}-textField`]: {
    width: '100%',
    marginTop: theme.spacing(0.5),
    '& .MuiInputBase-root': {
      backgroundColor: FsColors.base.background,
      color: FsColors.base.text,
      borderRadius: 0,
      '& fieldset': {
        borderColor: FsColors.base.border,
        borderRadius: 0,
      },
      '&:hover fieldset': {
        borderColor: FsColors.base.textSecondary,
      },
      '&.Mui-focused fieldset': {
        border: `1px solid ${FsColors.base.text}`
      },
    },
    '& .MuiInputBase-input': {
      color: FsColors.base.text,
      ...theme.typography.caption,
      '&::placeholder': {
        color: FsColors.base.textSecondary,
        opacity: 1,
        ...theme.typography.caption,
      },
    },
    '& .MuiInputBase-input.MuiOutlinedInput-input.MuiInputBase-inputMultiline': {
      padding: 'unset',
    },
    '& .MuiInputLabel-root': {
      color: FsColors.base.text,
      ...theme.typography.caption,
    },
  },

  [`& .${MUI_NAME}-spacer`]: {
    marginBottom: theme.spacing(0.5),
  },

  [`& .${MUI_NAME}-commentsContainer`]: {
    display: 'flex',
    flexDirection: 'column',
  },

  [`& .${MUI_NAME}-noComments`]: {
    ...theme.typography.caption,
    color: FsColors.base.textSecondary,
    fontStyle: 'italic',
  },

  [`& .${MUI_NAME}-commentItem`]: {
    display: 'flex',
    flexDirection: 'column',
    marginBottom: theme.spacing(1),
  },

  [`& .${MUI_NAME}-divider`]: {
    borderColor: FsColors.base.border,
  },

  [`& .${MUI_NAME}-commentContent`]: {
    ...theme.typography.caption,
    color: FsColors.base.text,
  },

  [`& .${MUI_NAME}-commentMeta`]: {
    display: 'flex',
    justifyContent: 'flex-end',

    '& .MuiTypography-root': {
      ...theme.typography.caption,
      color: FsColors.base.textSecondary,
      fontStyle: 'italic',
    },
  },
}));