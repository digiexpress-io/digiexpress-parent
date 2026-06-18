import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';
import { OwnerState } from './useOwnerState';



const MUI_NAME = 'FsPanelChanges';

export interface FsPanelChangesClasses {
  root: string;
  changeRow: string;
  assetName: string;
  assetTitle: string;
  assetPath: string;
  undoButton: string;
  actionBar: string;
}

export type FsPanelChangesClassKey = keyof FsPanelChangesClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    changeRow: ['changeRow'],
    assetName: ['assetName'],
    assetTitle: ['assetTitle'],
    assetPath: ['assetPath'],
    undoButton: ['undoButton'],
    actionBar: ['actionBar'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};


export const FsPanelChangesRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => ({
  display: 'flex',
  flexDirection: 'column',

  [`& .${MUI_NAME}-changeRow:nth-of-type(odd)`]: {
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.surface : FsColors.light.surface,
  },

  [`& .${MUI_NAME}-changeRow`]: {
    display: 'flex',
    alignItems: 'center',
    gap: theme.spacing(1),
    paddingLeft: theme.spacing(1),
    paddingBottom: theme.spacing(0.5),
    paddingTop: theme.spacing(0.5),
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.background : FsColors.light.background,
    borderBottom: `1px solid ${ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border}`,
  },

  [`& .${MUI_NAME}-assetTitle`]: {
    ...theme.typography.subtitle2,
    fontWeight: 'bold',
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
  },

  [`& .${MUI_NAME}-assetPath`]: {
    ...theme.typography.caption,
    color: ownerState.isDarkMode ? FsColors.dark.textSecondary : FsColors.light.textSecondary,
  },

  [`& .${MUI_NAME}-assetName`]: {
    fontWeight: 500,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    flex: 1,
    ...theme.typography.subtitle2
  },

  [`& .${MUI_NAME}-undoButton`]: {
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    '&:hover': {
      backgroundColor: ownerState.isDarkMode ? FsColors.dark.surface : FsColors.light.surface
    }
  },

  [`& .${MUI_NAME}-actionBar`]: {
    display: 'flex',
    justifyContent: 'flex-end',
    gap: theme.spacing(1),
    marginBottom: theme.spacing(1)
  },

}));
