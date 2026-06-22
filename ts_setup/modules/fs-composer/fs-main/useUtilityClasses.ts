import composeClasses from '@mui/utils/composeClasses';
import { darken, generateUtilityClass, styled } from '@mui/material';
import { FsColors } from '../fs-theme';
import { OwnerState } from './useOwnerState';

export const MUI_NAME = 'FsMain';
const toolbarWidth = '50px';

export interface FsMainClasses {
  root: string;
  leftPanel: string;
  divider: string;
  rightPanel: string;
  rightPanelOpen: string;
  rightPanelClosed: string;
  rightPanelContent: string;
  toolbar: string;
}

export type FsMainClassKey = keyof FsMainClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    leftPanel: ['leftPanel'],
    divider: ['divider'],
    rightPanel: ['rightPanel'],
    rightPanelOpen: ['rightPanelOpen'],
    rightPanelClosed: ['rightPanelClosed'],
    rightPanelContent: ['rightPanelContent'],
    toolbar: ['toolbar'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsMainRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => {
  return {
    display: 'flex',
    height: '100vh',
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.background : FsColors.light.background,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,

    [`& .${MUI_NAME}-leftPanel`]: {
      flex: 1,
      minWidth: 0,
      padding: theme.spacing(2),
      backgroundColor: ownerState.isDarkMode ? FsColors.dark.surface : FsColors.light.background,
      color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
      overflow: 'auto'
    },

    [`& .${MUI_NAME}-divider`]: {
      width: '1px',
      height: '100%',
      backgroundColor: ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border,
      flexShrink: 0
    },

    [`& .${MUI_NAME}-rightPanel`]: {
      height: '100%',
      width: ownerState.isRightPanelOpen ? `calc(50% - ${toolbarWidth})` : '0px',
      transition: 'width 300ms cubic-bezier(0.4, 0, 0.2, 1)',
      overflow: 'hidden',
      display: 'flex',
      flexShrink: 0
    },

    [`& .${MUI_NAME}-rightPanelContent`]: {
      width: '100%',
      height: '100%'
    },

    [`& .${MUI_NAME}-toolbar`]: {
      width: toolbarWidth,
      height: '100%',
      paddingTop: theme.spacing(1),
      paddingBottom: theme.spacing(1),
      display: 'flex',
      gap: theme.spacing(2),
      flexDirection: 'column',
      backgroundColor: ownerState.isDarkMode ? FsColors.dark.surface : FsColors.light.surface,
      borderLeft: `1px solid ${ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border}`,
      alignItems: 'center',
      flexShrink: 0
    }
  };
});


export const FsToolbarButtonRoot = styled('div', {
  name: MUI_NAME,
  slot: 'ToolbarButton',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: { isDarkMode: boolean, isSelected: boolean, isEnabled: boolean } }>(({ theme, ownerState }) => {

  return {

    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    opacity: 1,
    borderRadius: theme.spacing(0.5),
    padding: theme.spacing(0.5),
    backgroundColor: 'transparent',
    border: '1px solid transparent',
    '& .MuiSvgIcon-root': {
      fontSize: '1.2rem',
      color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
    },
    '&:hover': {
      backgroundColor: ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.surface
    },

    ...(ownerState.isSelected ? {
      backgroundColor: theme.palette.primary.main,
      border: `1px solid ${theme.palette.primary.main}`,
      '& .MuiSvgIcon-root': {
        color: theme.palette.primary.contrastText,
      },
      '&:hover': {
        backgroundColor: darken(theme.palette.primary.main, 0.6),
      },
    } : {}),

    ...(!ownerState.isEnabled ? {
      opacity: 0.3,
      cursor: 'default',
      pointerEvents: 'none',
    } : {}),
  };
});


export const FsSaveButtonRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Save',
  shouldForwardProp: (prop) => prop !== 'ownerState',
})<{ ownerState: { isDarkMode: boolean, unsavedCount: number } }>(({ theme, ownerState }) => {
  return {
    [`& .${MUI_NAME}-toolbarSaveButton.${MUI_NAME}-toolbarButtonSelected`]: {
      backgroundColor: ownerState.isDarkMode ? FsColors.semantic.primary + '26' : FsColors.semantic.dangerLight + '26',
      border: ownerState.isDarkMode ? `1px solid ${FsColors.semantic.dangerDark}` : `1px solid ${FsColors.semantic.dangerLight}`,
      '&:hover': {
        backgroundColor: ownerState.isDarkMode ? FsColors.semantic.primary + '40' : FsColors.semantic.warningLight + '40',
      },
    },
    cursor: ownerState.unsavedCount > 0 ? 'pointer' : 'default',
    pointerEvents: ownerState.unsavedCount > 0 ? 'auto' : 'none',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    opacity: ownerState.unsavedCount > 0 ? 1 : 0.3,
    borderRadius: theme.spacing(0.5),
    padding: theme.spacing(0.5),
    backgroundColor: 'transparent',
    border: '1px solid transparent',
    '& .MuiSvgIcon-root': {
      fontSize: '1.2rem',
      color: ownerState.unsavedCount > 0 ? theme.palette.error.main : (ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text),
    },
    '&:hover': {
      backgroundColor: ownerState.isDarkMode ? FsColors.dark.border + '20' : FsColors.light.surface
    },
    [`& .MuiBadge-badge`]: {
      fontSize: '10px',
      fontWeight: 'bold',
      height: theme.spacing(2),
      minWidth: theme.spacing(2),
    },
  };
});