import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';
import { OwnerState } from './useOwnerState';



const MUI_NAME = 'FsChanges';

export interface FsChangesClasses {
  root: string;
  changeRow: string;
  assetName: string;
  statusText: string;
  undoButton: string;
  actionBar: string;
  discardButton: string;
}

export type FsChangesClassKey = keyof FsChangesClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    changeRow: ['changeRow'],
    assetName: ['assetName'],
    statusText: ['statusText'],
    undoButton: ['undoButton'],
    actionBar: ['actionBar'],
    discardButton: ['discardButton'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};


export const FsChangesRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => ({

  display: 'flex',
  flexDirection: 'column',
  border: `1px solid ${ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border}`,

  [`& .${MUI_NAME}-changeRow:nth-of-type(odd)`]: {
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.surface : FsColors.light.surface,
  },

  [`& .${MUI_NAME}-changeRow`]: {
    display: 'flex',
    alignItems: 'center',
    gap: theme.spacing(2),
    padding: theme.spacing(1, 1.5),
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.background : FsColors.light.background,
    borderBottom: `1px solid ${ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border}`,
    '&:last-child': {
      borderBottom: 'none'
    }
  },

  [`& .${MUI_NAME}-assetName`]: {
    fontWeight: 500,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    width: '300px',
    flexShrink: 0,
    ...theme.typography.subtitle2
  },

  [`& .${MUI_NAME}-statusText`]: {
    fontWeight: 500,
    width: '100px',
    flexShrink: 0,
    textAlign: 'right',
    ...theme.typography.subtitle2
  },

  [`& .${MUI_NAME}-undoButton`]: {
    marginLeft: 'auto',
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    '&:hover': {
      backgroundColor: ownerState.isDarkMode ? FsColors.dark.surface : FsColors.light.surface
    }
  },

  [`& .${MUI_NAME}-actionBar`]: {
    display: 'flex',
    justifyContent: 'center',
    gap: theme.spacing(1),
    marginBottom: theme.spacing(1)
  },

  [`& .${MUI_NAME}-discardButton`]: {
    color: ownerState.isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight,
  },
}));