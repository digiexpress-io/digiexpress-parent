import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';
import { OwnerState } from './useOwnerState';

const MUI_NAME = 'FsHelp';

export interface FsHelpClasses {
  root: string;
  codeBlock: string;
  codeContent: string;
  externalLink: string;
  h1: string;
  h2: string;
  h3: string;
  h4: string;
  h5: string;
  h6: string;
  paragraph: string;
  listItem: string;
}

export type FsHelpClassKey = keyof FsHelpClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    codeBlock: ['codeBlock'],
    codeContent: ['codeContent'],
    externalLink: ['externalLink'],
    h1: ['h1'],
    h2: ['h2'],
    h3: ['h3'],
    h4: ['h4'],
    h5: ['h5'],
    h6: ['h6'],
    paragraph: ['paragraph'],
    listItem: ['listItem'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsHelpRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => ({

  [`& .${MUI_NAME}-h1`]: {
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    ...theme.typography.h1
  },

  [`& .${MUI_NAME}-h2`]: {
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
   ...theme.typography.h2
  },

  [`& .${MUI_NAME}-h3`]: {
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    ...theme.typography.h3
  },

  [`& .${MUI_NAME}-h4`]: {
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
   ...theme.typography.h4
  },

  [`& .${MUI_NAME}-h5`]: {
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    ...theme.typography.h5
  },

  [`& .${MUI_NAME}-h6`]: {
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    ...theme.typography.h6
  },

  [`& .${MUI_NAME}-paragraph`]: {
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    ...theme.typography.body1
  },

  [`& .${MUI_NAME}-listItem`]: {
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    marginBottom: theme.spacing(0.25),
    ...theme.typography.body1
  },

  [`& .${MUI_NAME}-codeBlock`]: {
    ...theme.typography.subtitle2,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
  },

  [`& .${MUI_NAME}-codeContent`]: {
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.surface : FsColors.light.surface,
    padding: theme.spacing(1),
  },

  [`& .${MUI_NAME}-externalLink`]: {
    display: 'flex',
    alignItems: 'center',
    gap: theme.spacing(0.5),
    marginTop: theme.spacing(0.125),
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    textDecoration: 'none',
    '&:hover': {
      textDecoration: 'underline',
    },
  },
}));