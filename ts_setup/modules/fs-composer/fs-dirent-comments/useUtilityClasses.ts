import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';
import { OwnerState } from './useOwnerState';

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
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => ({
  display: 'flex',
  flexDirection: 'column',

  [`& .${MUI_NAME}-title`]: {
    ...theme.typography.caption,
    fontWeight: 500,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
  },

  [`& .${MUI_NAME}-textField`]: {
    width: '100%',
    marginTop: theme.spacing(0.5),
    '& .MuiInputBase-root': {
      backgroundColor: ownerState.isDarkMode ? FsColors.dark.background : FsColors.light.background,
      color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
      borderRadius: 0,
      '& fieldset': {
        borderColor: ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border,
        borderRadius: 0,
      },
      '&:hover fieldset': {
        borderColor: ownerState.isDarkMode ? FsColors.light.textSecondary : FsColors.dark.textSecondary,
      },
      '&.Mui-focused fieldset': {
        border: `1px solid ${ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text}`
      },
    },
    '& .MuiInputBase-input': {
      color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
      ...theme.typography.caption,
      '&::placeholder': {
        color: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
        opacity: 1,
        ...theme.typography.caption,
      },
    },
    '& .MuiInputBase-input.MuiOutlinedInput-input.MuiInputBase-inputMultiline': {
      padding: 'unset',
    },
    '& .MuiInputLabel-root': {
      color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
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
    color: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
    fontStyle: 'italic',
  },

  [`& .${MUI_NAME}-commentItem`]: {
    display: 'flex',
    flexDirection: 'column',
    marginBottom: theme.spacing(1),
  },

  [`& .${MUI_NAME}-divider`]: {
    borderColor: ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border,
  },

  [`& .${MUI_NAME}-commentContent`]: {
    ...theme.typography.caption,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
  },

  [`& .${MUI_NAME}-commentMeta`]: {
    display: 'flex',
    justifyContent: 'flex-end',

    '& .MuiTypography-root': {
      ...theme.typography.caption,
      color: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
      fontStyle: 'italic',
    },
  },
}));