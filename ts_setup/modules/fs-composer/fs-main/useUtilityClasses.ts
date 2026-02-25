import composeClasses from '@mui/utils/composeClasses';
import { generateUtilityClass, styled } from '@mui/material';
import { FsColors } from '../fs-theme';
import { OwnerState } from './useOwnerState';
import { FsMainProps } from './FsMainProps';

export const MUI_NAME = 'FsMain';

export interface FsMainClasses {
  root: string;
  leftPanel: string;
  divider: string;
  rightPanel: string;
  rightPanelOpen: string;
  rightPanelClosed: string;
  rightPanelContent: string;
  toolbar: string;
  toolbarButton: string;
  toolbarButtonSelected: string;
  toolbarSaveButton: string;
}

export type FsMainClassKey = keyof FsMainClasses;

export const useUtilityClasses = (_props: FsMainProps) => {
  const slots = {
    root: ['root'],
    leftPanel: ['leftPanel'],
    divider: ['divider'],
    rightPanel: ['rightPanel'],
    rightPanelOpen: ['rightPanelOpen'],
    rightPanelClosed: ['rightPanelClosed'],
    rightPanelContent: ['rightPanelContent'],
    toolbar: ['toolbar'],
    toolbarButton: ['toolbarButton'],
    toolbarButtonSelected: ['toolbarButtonSelected'],
    toolbarSaveButton: ['toolbarSaveButton']
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
    height: '100%',
    width: '100%',
    backgroundColor: ownerState.isDarkMode ? FsColors.dark.background : FsColors.light.background,
    color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,

    [`& .${MUI_NAME}-leftPanel`]: {
      flex: 1,
      minWidth: 0,
      padding: theme.spacing(2),
      backgroundColor: ownerState.isDarkMode ? FsColors.dark.surface : FsColors.light.surface,
      color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
      overflow: 'auto',

      '& .MuiTypography-root': {
        ...theme.typography.caption,
        fontWeight: 500,
      }
    },

    [`& .${MUI_NAME}-divider`]: {
      width: '1px',
      height: '100%',
      backgroundColor: ownerState.isDarkMode ? FsColors.dark.border : FsColors.light.border,
      flexShrink: 0
    },

    [`& .${MUI_NAME}-rightPanel`]: {
      height: '100%',
      width: ownerState.isRightPanelOpen ? `calc(50% - ${ownerState.toolbar.width})` : '0px',
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
      width: ownerState.toolbar.width,
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
    },

    [`& .${MUI_NAME}-toolbarButton`]: {
      cursor: 'pointer',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      opacity: 1,
      borderRadius: '4px',
      padding: '4px',
      backgroundColor: 'transparent',
      border: '1px solid transparent',
      '& .MuiSvgIcon-root': {
        fontSize: '1.2rem',
        color: ownerState.isDarkMode ? FsColors.dark.text : FsColors.light.text,
      },
      '&:hover': {
        backgroundColor: ownerState.isDarkMode ? FsColors.dark.border + '20' : FsColors.light.surface
      },
    },

    [`& .${MUI_NAME}-toolbarButtonSelected`]: {
      backgroundColor: ownerState.isDarkMode ? FsColors.semantic.primary + '26' : FsColors.semantic.warningLight + '26',
      border: ownerState.isDarkMode ? `1px solid ${FsColors.semantic.primary}` : `1px solid ${FsColors.semantic.warningLight}`,
      '& .MuiSvgIcon-root': {
        color: ownerState.isDarkMode ? FsColors.semantic.primary : FsColors.semantic.warningLight,
      },
      '&:hover': {
        backgroundColor: ownerState.isDarkMode ? FsColors.semantic.primary + '40' : FsColors.semantic.warningLight + '40',
      },
    },

    [`& .${MUI_NAME}-toolbarSaveButton`]: {
      cursor: 'pointer',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      opacity: 1,
      borderRadius: '4px',
      padding: '4px',
      backgroundColor: 'transparent',
      border: '1px solid transparent',
      '& .MuiSvgIcon-root': {
        fontSize: '1.2rem',
        color: theme.palette.error.main
      },
      '&:hover': {
        backgroundColor: ownerState.isDarkMode ? FsColors.dark.border + '20' : FsColors.light.surface
      },
    },

    [`& .${MUI_NAME}-toolbarSaveButton.${MUI_NAME}-toolbarButtonSelected`]: {
      backgroundColor: ownerState.isDarkMode ? FsColors.semantic.primary + '26' : FsColors.semantic.dangerLight + '26',
      border: ownerState.isDarkMode ? `1px solid ${FsColors.semantic.dangerDark}` : `1px solid ${FsColors.semantic.dangerLight}`,
      '&:hover': {
        backgroundColor: ownerState.isDarkMode ? FsColors.semantic.primary + '40' : FsColors.semantic.warningLight + '40',
      },
    },
  };
});