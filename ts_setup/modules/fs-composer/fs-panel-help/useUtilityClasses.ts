import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';

const MUI_NAME = 'FsPanelHelp';

export interface FsPanelHelpClasses {
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

export type FsPanelHelpClassKey = keyof FsPanelHelpClasses;

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

export const FsPanelHelpRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})(({ theme }) => ({

  [`& .${MUI_NAME}-h1`]: {
    color: FsColors.base.text,
    ...theme.typography.h1
  },

  [`& .${MUI_NAME}-h2`]: {
    color: FsColors.base.text,
   ...theme.typography.h2
  },

  [`& .${MUI_NAME}-h3`]: {
    color: FsColors.base.text,
    ...theme.typography.h3
  },

  [`& .${MUI_NAME}-h4`]: {
    color: FsColors.base.text,
   ...theme.typography.h4
  },

  [`& .${MUI_NAME}-h5`]: {
    color: FsColors.base.text,
    ...theme.typography.h5
  },

  [`& .${MUI_NAME}-h6`]: {
    color: FsColors.base.text,
    ...theme.typography.h6
  },

  [`& .${MUI_NAME}-paragraph`]: {
    color: FsColors.base.text,
    ...theme.typography.body1
  },

  [`& .${MUI_NAME}-listItem`]: {
    color: FsColors.base.text,
    marginBottom: theme.spacing(0.25),
    ...theme.typography.body1
  },

  [`& .${MUI_NAME}-codeBlock`]: {
    ...theme.typography.subtitle2,
    color: FsColors.base.text,
  },

  [`& .${MUI_NAME}-codeContent`]: {
    backgroundColor: FsColors.base.surface,
    padding: theme.spacing(1),
  },

  [`& .${MUI_NAME}-externalLink`]: {
    display: 'flex',
    alignItems: 'center',
    gap: theme.spacing(0.5),
    marginTop: theme.spacing(0.125),
    color: FsColors.base.text,
    textDecoration: 'none',
    '&:hover': {
      textDecoration: 'underline',
    },
  },
}));
