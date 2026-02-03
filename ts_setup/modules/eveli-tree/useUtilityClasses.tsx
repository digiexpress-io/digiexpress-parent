import composeClasses from '@mui/utils/composeClasses';
import { generateUtilityClass, styled } from '@mui/material';

export const MUI_NAME = 'EveliTree';

export interface EveliTreeClasses {
  root: string;
  title: string;
  titleText: string;
}

export type EveliTreeClassKey = keyof EveliTreeClasses;

export const useUtilityClasses = (isDarkTheme: boolean = false) => {
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
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.title,
      styles.titleText
    ];
  },
})<{ isDarkTheme?: boolean }>(({ theme, isDarkTheme }) => {
  return {
    backgroundColor: isDarkTheme ? '#1e1e1e' : '#ffffff',
    color: isDarkTheme ? '#cccccc' : '#333333',
    height: '100%',
    maxHeight: '100vh',
    fontSize: '13px',
    overflow: 'auto',

    [`& .${MUI_NAME}-title`]: {
      borderBottom: isDarkTheme ? '1px solid #3c3c3c' : '1px solid #e0e0e0',
      backgroundColor: isDarkTheme ? '#2d2d30' : '#f5f5f5',
      padding: theme.spacing(1),
      display: 'flex',
      alignItems: 'center',
      width: '100%'
    },

    [`& .${MUI_NAME}-titleText`]: {
      color: isDarkTheme ? theme.palette.background.default : '#333333',
      ...theme.typography.body1
    },
  };
});
