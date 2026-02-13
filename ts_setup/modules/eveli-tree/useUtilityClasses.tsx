import composeClasses from '@mui/utils/composeClasses';
import { generateUtilityClass, styled } from '@mui/material';
import { getThemeColors } from './tree-theme';

export const MUI_NAME = 'EveliTree';

export interface EveliTreeClasses {
  root: string;
  title: string;
  titleText: string;
}

export type EveliTreeClassKey = keyof EveliTreeClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    titleText: ['titleText']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const EveliTreeRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'isDarkTheme',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.title,
      styles.titleText
    ];
  },
})<{ isDarkTheme: boolean }>(({ theme, isDarkTheme }) => {
  const treeThemeColors = getThemeColors(isDarkTheme);

  return {
    backgroundColor: treeThemeColors.background,
    color: treeThemeColors.text,
    height: '100%',
    maxHeight: '100vh',
    fontSize: '13px',
    overflow: 'auto',

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
  };
});
