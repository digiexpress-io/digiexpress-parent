import composeClasses from '@mui/utils/composeClasses';
import { generateUtilityClass, styled, Menu } from '@mui/material';

export const MUI_NAME = 'EveliTreeItemMenu';
export const MENU_WIDTH = 350;
export const MENU_WIDTH_EXTENDED = MENU_WIDTH * 3;
export const MENU_HEIGHT = 800; // Approximate height when submenu is open

export interface EveliTreeItemMenuClasses {
  root: string;
  nodeNameContainer: string;
  menuItem: string;
  menuItemActive: string;
  menuItemDelete: string;
  divider: string;
  textField: string;
  label: string;
  expandedContent: string;
  menuContainer: string;
  leftMenuSection: string;
  menuDivider: string;
  submenuSection: string;
}

export type EveliTreeItemMenuClassKey = keyof EveliTreeItemMenuClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    nodeNameContainer: ['nodeNameContainer'],
    menuItem: ['menuItem'],
    menuItemActive: ['menuItemActive'],
    menuItemDelete: ['menuItemDelete'],
    divider: ['divider'],
    textField: ['textField'],
    label: ['label'],
    expandedContent: ['expandedContent'],
    menuContainer: ['menuContainer'],
    leftMenuSection: ['leftMenuSection'],
    menuDivider: ['menuDivider'],
    submenuSection: ['submenuSection']
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
      styles.menuItemActive,
      styles.menuItemDelete,
      styles.divider,
      styles.textField,
      styles.label,
      styles.expandedContent,
      styles.menuContainer,
      styles.leftMenuSection,
      styles.menuDivider,
      styles.submenuSection
    ];
  },
})<{ isSubmenuOpen?: boolean }>(({ theme }) => {
  return {
    // Menu paper styles
    '& .MuiPaper-root': {
      backgroundColor: '#2d2d30',
      color: '#cccccc',
      border: '1px solid #3c3c3c',
      minWidth: MENU_WIDTH,
      width: 'auto',
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

    [`& .${MUI_NAME}-menuItemActive`]: {
      fontSize: '13px',
      display: 'flex',
      alignItems: 'center',
      gap: theme.spacing(1),
      backgroundColor: '#4c4b4b',
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

    [`& .${MUI_NAME}-menuContainer`]: {
      display: 'flex',
      width: '100%',
      alignItems: 'flex-start',
    },

    [`& .${MUI_NAME}-leftMenuSection`]: {
      width: MENU_WIDTH,
      overflow: 'visible',
      display: 'flex',
      flexDirection: 'column',
      flexShrink: 0,
    },

    [`& .${MUI_NAME}-menuDivider`]: {
      borderColor: '#3c3c3c',
      margin: '0',
    },

    // Collapse component styling to ensure proper flex behavior
    '& .MuiCollapse-root': {
      display: 'flex',
      alignItems: 'flex-start',
    },
    '& .MuiCollapse-wrapper': {
      display: 'flex',
      alignItems: 'flex-start',
    },
    '& .MuiCollapse-wrapperInner': {
      display: 'flex',
      alignItems: 'flex-start',
    },

    [`& .${MUI_NAME}-submenuSection`]: {
      width: MENU_WIDTH,
      maxHeight: MENU_HEIGHT,
      padding: theme.spacing(2),
      overflowY: 'auto',
      overflowX: 'hidden',
    },
  };
});