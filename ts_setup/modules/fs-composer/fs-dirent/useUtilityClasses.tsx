import { generateUtilityClass, styled, alpha } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors, getDirentColor } from '../fs-theme';



export const MUI_NAME = 'Fs';

export interface FsDirentClasses {
  root: string;
  explorerDirent: string;
  explorerDirentContent: string;
  icon: string;

  iconFolder: string;
  iconArticle: string;
  iconService: string;
  iconDialob: string;
  iconFlow: string;
  iconLink: string;
  iconLanguage: string;
  iconPrintout: string;
  iconImage: string;
  iconTemplate: string;
  iconPhone: string;
  iconPage: string;
  iconExpand: string;
  iconConfig: string;
  direntName: string;
}

export type FsDirentClassKey = keyof FsDirentClasses;

export const useUtilityClasses = (_isDarkTheme: boolean) => {
  const slots = {
    root: ['root'],
    explorerDirent: ['explorerDirent'],
    explorerDirentContent: ['explorerDirentContent'],
    icon: ['icon'],
    iconFolder: ['iconFolder'],
    iconArticle: ['iconArticle'],
    iconService: ['iconService'],
    iconDialob: ['iconDialob'],
    iconFlow: ['iconFlow'],
    iconLink: ['iconLink'],
    iconLanguage: ['iconLanguage'],
    iconPrintout: ['iconPrintout'],
    iconImage: ['iconImage'],
    iconTemplate: ['iconTemplate'],
    iconPhone: ['iconPhone'],
    iconPage: ['iconPage'],
    iconExpand: ['iconExpand'],
    iconConfig: ['iconConfig'],
    direntName: ['direntName'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsDirentRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'ownerState'
})<{ ownerState: any }>(({ theme, ownerState }) => {
  const isDarkTheme = ownerState.isDarkMode;

  return {

    [`& .${MUI_NAME}-icon`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      fontSize: '15px',
    },

    [`& .${MUI_NAME}-iconFolder`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: getDirentColor('folder', isDarkTheme),
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconArticle`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: getDirentColor('article', isDarkTheme),
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconService`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: getDirentColor('service', isDarkTheme),
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconDialob`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: getDirentColor('dialob', isDarkTheme),
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconFlow`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: getDirentColor('flow', isDarkTheme),
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconLink`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: getDirentColor('link', isDarkTheme),
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconLanguage`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: getDirentColor('language', isDarkTheme),
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconPrintout`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: getDirentColor('printout', isDarkTheme),
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconImage`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: getDirentColor('image', isDarkTheme),
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconTemplate`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: getDirentColor('template', isDarkTheme),
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconPhone`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: getDirentColor('phone', isDarkTheme),
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconPage`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: getDirentColor('page', isDarkTheme),
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

    [`& .${MUI_NAME}-explorerDirent`]: {
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
    },
  };
});





