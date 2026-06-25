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
  truncationWrapper: string;
}

export type FsDirentClassKey = keyof FsDirentClasses;

export const useUtilityClasses = (dirent: Fs.DirentBase) => {
  const slots = {
    root: ['root'],
    explorerDirent: ['explorerDirent'],
    explorerDirentContent: ['explorerDirentContent'],
    iconExpand: ['iconExpand'],
    iconConfig: ['iconConfig'],
    direntName: ['direntName'],
    truncationWrapper: ['truncationWrapper'],
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
  const widget = createWidget(ownerState.dirent)

  return {

    paddingLeft: theme.spacing(ownerState.level * 1.2),
    cursor: 'pointer',
    '&:hover': {
      backgroundColor: FsColors.base.surface,
    },
    '&.active': {
      backgroundColor: alpha(FsColors.semantic.primary, 0.1),
      outline: `1px solid ${alpha(FsColors.semantic.primary, 0.5)}`,
    },
    '&.error': {
      backgroundColor: alpha(FsColors.semantic.danger, 0.1),
      '&:hover': {
        backgroundColor: alpha(FsColors.semantic.danger, 0.2),
      },
    },

    [`& .${MUI_NAME}-${widget.classNames.icon}`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: widget.colors.dirent,
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },
    [`& .${MUI_NAME}-iconExpand`]: {
      fontSize: '15px',
      color: FsColors.base.textSecondary,
    },

    [`& .${MUI_NAME}-iconConfig`]: {
      fontSize: '14px',
      color: FsColors.base.text,
    },

    [`& .${MUI_NAME}-explorerDirentContent`]: {
      display: 'flex',
      alignItems: 'center',
      width: '100%',
      overflow: 'hidden',
    },

    [`& .${MUI_NAME}-truncationWrapper`]: {
      minWidth: 0,
      overflow: 'hidden',
      flex: 1,
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


