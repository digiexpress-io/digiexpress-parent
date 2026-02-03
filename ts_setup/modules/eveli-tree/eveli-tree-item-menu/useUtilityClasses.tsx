import composeClasses from '@mui/utils/composeClasses';
import { generateUtilityClass, styled, Menu } from '@mui/material';

export const MUI_NAME = 'EveliTreeItemMenu';
export const MENU_WIDTH = 350;

export interface EveliTreeItemMenuClasses {
  root: string;
  nodeNameContainer: string;
  menuItem: string;
  menuItemDelete: string;
  divider: string;
  textField: string;
  label: string;
  expandedContent: string;
}

export type EveliTreeItemMenuClassKey = keyof EveliTreeItemMenuClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    nodeNameContainer: ['nodeNameContainer'],
    menuItem: ['menuItem'],
    menuItemDelete: ['menuItemDelete'],
    divider: ['divider'],
    textField: ['textField'],
    label: ['label'],
    expandedContent: ['expandedContent']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const EveliTreeItemMenuRoot = styled(Menu, {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.nodeNameContainer,
      styles.menuItem,
      styles.menuItemDelete,
      styles.divider,
      styles.textField,
      styles.label,
      styles.expandedContent
    ];
  },
})(({ theme }) => {
  return {
    // Menu paper styles
    '& .MuiPaper-root': {
      backgroundColor: '#2d2d30',
      color: '#cccccc',
      border: '1px solid #3c3c3c',
      minWidth: MENU_WIDTH,
      padding: theme.spacing(0.5)
    },
    [`& .${MUI_NAME}-nodeNameContainer`]: {
      padding: theme.spacing(0.5, 2, 0.5, 2),
      '& .MuiTypography-subtitle2': {
        color: '#cccccc',
        fontWeight: 500,
      },
      '& .MuiTypography-caption': {
        color: '#888888',
      },
    },

    [`& .${MUI_NAME}-menuItem`]: {
      fontSize: '13px',
      display: 'flex',
      alignItems: 'center',
      gap: theme.spacing(1),
      '&:hover': {
        backgroundColor: '#3c3c3c',
      },
      '& .MuiSvgIcon-root': {
        color: '#cccccc',
        fontSize: '16px',
      },
    },

    [`& .${MUI_NAME}-menuItemDelete`]: {
      fontSize: '13px',
      color: '#f48771',
      display: 'flex',
      alignItems: 'center',
      gap: '8px',
      '&:hover': {
        backgroundColor: '#3c3c3c',
      },
      '& .MuiSvgIcon-root': {
        color: '#f48771',
        fontSize: '16px',
      },
    },

    [`& .${MUI_NAME}-divider`]: {
      borderColor: '#3c3c3c',
      margin: '0px !important',
    },

    [`& .${MUI_NAME}-textField`]: {
      width: '100%',
      '& .MuiInputBase-root': {
        backgroundColor: '#1e1e1e',
        color: '#cccccc',
        ...theme.typography.caption,
        borderRadius: 0,
      },
      '& .MuiInputBase-input': {
        color: '#cccccc',
        padding: '0px'
      },
    },

    [`& .${MUI_NAME}-label`]: {
      backgroundColor: '#3c3c3c',
      color: '#cccccc',
      fontSize: '10px',
      height: '18px',
      border: '1px solid #555555',
      '& .MuiChip-label': {
        padding: '0 6px',
      },
      '& .MuiSvgIcon-root': {
        fontSize: '14px',
        color: '#ffa500'
      }
    },

    [`& .${MUI_NAME}-expandedContent`]: {
      padding: theme.spacing(0, 2, 1, 2),
      '& .MuiTypography-body2': {
        color: '#888888',
        fontStyle: 'italic',
      },
    },
  };
});