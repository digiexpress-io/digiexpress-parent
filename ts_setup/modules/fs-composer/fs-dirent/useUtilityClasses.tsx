import { generateUtilityClass, styled, alpha, ListItem } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';
import { createWidget } from '../fs-factory';

import { Fs } from '@dxs-ts/fs-api';


export const MUI_NAME = 'Fs';

export interface FsDirentClasses {
  root: string;
  explorerDirent: string;
  explorerDirentContent: string;
  iconExpand: string;
  iconConfig: string;
  direntName: string;
}

export type FsDirentClassKey = keyof FsDirentClasses;

export const useUtilityClasses = (_isDarkTheme: boolean, dirent: Fs.DirentBase) => {
  const slots = {
    root: ['root'],
    explorerDirent: ['explorerDirent'],
    explorerDirentContent: ['explorerDirentContent'], 
    iconExpand: ['iconExpand'],
    iconConfig: ['iconConfig'],
    direntName: ['direntName'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return {
    ...composeClasses(slots, getUtilityClass, {}),
    direntIcon: generateUtilityClass(MUI_NAME, createWidget(dirent).classNames.icon),
  };
};

export const FsDirentRoot = styled(ListItem, {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState'
})<{ ownerState: any }>(({ theme, ownerState }) => {
  const isDarkTheme = ownerState.isDarkMode;
  const widget = createWidget(ownerState.dirent)

  return {

    paddingLeft: theme.spacing(ownerState.level * 1.2),
    cursor: 'pointer',
    '&:hover': {
      backgroundColor: isDarkTheme ? FsColors.dark.surface : FsColors.light.surface,
    },
    '&.active': {
      backgroundColor: isDarkTheme ? alpha(FsColors.semantic.primary, 0.15) : alpha(FsColors.semantic.primary, 0.1),
      outline: `1px solid ${isDarkTheme ? alpha(FsColors.semantic.primary, 0.6) : alpha(FsColors.semantic.primary, 0.5)}`,
    },
    '&.error': {
      backgroundColor: alpha(isDarkTheme ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight, 0.1),
      '&:hover': {
        backgroundColor: isDarkTheme ? alpha(FsColors.semantic.dangerDark, 0.3) : alpha(FsColors.semantic.dangerLight, 0.2),
      },
    },

    [`& .${MUI_NAME}-${widget.classNames.icon}`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: isDarkTheme ? widget.colors.direntDark : widget.colors.direntLight,
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },
    [`& .${MUI_NAME}-iconExpand`]: {
      fontSize: '15px',
      color: isDarkTheme ? FsColors.dark.text : FsColors.light.textSecondary,
    },

    [`& .${MUI_NAME}-iconConfig`]: {
      fontSize: '14px',
      color: isDarkTheme ? FsColors.dark.text : FsColors.light.text,
    },

    [`& .${MUI_NAME}-explorerDirentContent`]: {
      display: 'flex',
      alignItems: 'center',
      width: '100%',
      overflow: 'hidden',
    },

    [`& .${MUI_NAME}-direntName`]: {
      minWidth: 0,
      overflow: 'hidden',
      '& .MuiTypography-root': {
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap',
      },
    }
  }
});


