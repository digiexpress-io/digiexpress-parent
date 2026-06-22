import composeClasses from '@mui/utils/composeClasses';
import { generateUtilityClass, styled } from '@mui/material';
import { getThemeColors } from '../fs-theme';

export const MUI_NAME = 'FsExplorer';

export interface FsExplorerClasses {
  root: string;
  title: string;
  titleText: string;
  iconDark: string;
  iconLight: string;
  badgeDark: string;
  badgeLight: string;
  noSearchResults: string;
}

export type FsExplorerClassKey = keyof FsExplorerClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    titleText: ['titleText'],
    iconDark: ['iconDark'],
    iconLight: ['iconLight'],
    badgeDark: ['badgeDark'],
    badgeLight: ['badgeLight'],
    noSearchResults: ['noSearchResults']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const FsExplorerRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
})(({ theme }) => {
  const treeThemeColors = getThemeColors();

  return {
    backgroundColor: theme.palette.secondary.main,
    color: treeThemeColors.text,
    flex: 1,
    minHeight: 0,
    fontSize: '13px',
    overflowY: 'auto',
    overflowX: 'hidden',

    [`& .${MUI_NAME}-title`]: {
      borderBottom: `1px solid ${treeThemeColors.border}`,
      backgroundColor: treeThemeColors.surface,
      padding: theme.spacing(1),
      display: 'flex',
      alignItems: 'center',
      width: '100%'
    },

    [`& .${MUI_NAME}-titleText`]: {
      color: treeThemeColors.text,
      ...theme.typography.subtitle2,
      fontWeight: 500
    },

    [`& .${MUI_NAME}-iconDark`]: {
      size: 'small',
      color: treeThemeColors.text,
    },

    [`& .${MUI_NAME}-iconLight`]: {
      size: 'small',
      color: treeThemeColors.text,
    },

    [`& .${MUI_NAME}-badgeDark .MuiBadge-badge`]: {
      backgroundColor: treeThemeColors.text,
      color: treeThemeColors.surface,
      height: '10px',
      width: '10px',
      minWidth: '10px',
      borderRadius: '50%',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '2px',
      right: '2px',
      bottom: '2px',
    },

    [`& .${MUI_NAME}-badgeLight .MuiBadge-badge`]: {
      backgroundColor: treeThemeColors.text,
      color: treeThemeColors.background,
      height: '10px',
      width: '10px',
      minWidth: '10px',
      borderRadius: '50%',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '2px',
      right: '2px',
      bottom: '2px',
    },

    [`& .${MUI_NAME}-noSearchResults`]: {
      padding: theme.spacing(2),
      color: treeThemeColors.text,
      ...theme.typography.subtitle2,
      fontStyle: 'italic',
      fontWeight: 500,
    },
  };
});
