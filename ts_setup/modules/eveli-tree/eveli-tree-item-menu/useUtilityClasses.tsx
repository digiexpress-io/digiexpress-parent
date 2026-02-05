import composeClasses from '@mui/utils/composeClasses';
import { generateUtilityClass, styled, Popover } from '@mui/material';
import { TreeColors } from '../tree-theme';

export const MUI_NAME = 'EveliTreeItemMenu';
export const MENU_WIDTH = 350;
export const MENU_WIDTH_EXTENDED = MENU_WIDTH * 2;
export const MENU_HEIGHT = 700; // Approximate height when submenu is open
export const MENU_PADDING = 8; // theme.spacing(0.5) * 2 for top and bottom padding

export interface EveliTreeItemMenuClasses {
  root: string;
  headerMain: string;
  menuItem: string;
  menuItemActive: string;
  menuItemDelete: string;
  menuItemLocked: string;
  menuItemUnlocked: string;
  divider: string;
  textField: string;
  label: string;
  expandedContent: string;
  menuContainer: string;
  sectionMain: string;
  dividerSub: string;
  sectionSub: string;
}

export type EveliTreeItemMenuClassKey = keyof EveliTreeItemMenuClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    headerMain: ['headerMain'],
    menuItem: ['menuItem'],
    menuItemActive: ['menuItemActive'],
    menuItemDelete: ['menuItemDelete'],
    menuItemLocked: ['menuItemLocked'],
    menuItemUnlocked: ['menuItemUnlocked'],
    divider: ['divider'],
    textField: ['textField'],
    label: ['label'],
    expandedContent: ['expandedContent'],
    menuContainer: ['menuContainer'],
    sectionMain: ['sectionMain'],
    dividerSub: ['dividerSub'],
    sectionSub: ['sectionSub']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const EveliTreeItemMenuRoot = styled(Popover, {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => !['isSubmenuOpen', 'shouldExpandUpward'].includes(prop as string),
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.headerMain,
      styles.menuItem,
      styles.menuItemActive,
      styles.menuItemDelete,
      styles.menuItemLocked,
      styles.menuItemUnlocked,
      styles.divider,
      styles.textField,
      styles.label,
      styles.expandedContent,
      styles.menuContainer,
      styles.sectionMain,
      styles.dividerSub,
      styles.sectionSub
    ];
  },
})<{ isSubmenuOpen?: boolean; shouldExpandUpward?: boolean }>(({ theme, isSubmenuOpen }) => {
  return {
    // Menu paper styles
    '& .MuiPaper-root': {
      backgroundColor: TreeColors.dark.surface,
      color: TreeColors.dark.text,
      border: `1px solid ${TreeColors.dark.border}`,
      minWidth: MENU_WIDTH,
      width: isSubmenuOpen ? MENU_WIDTH_EXTENDED : 'auto',
      height: MENU_HEIGHT,
      maxHeight: MENU_HEIGHT,
      padding: theme.spacing(0.5),
      overflow: 'hidden',
      transition: 'width 0.3s ease-out'
    },
    [`& .${MUI_NAME}-headerMain`]: {
      padding: theme.spacing(0.5, 2, 0.5, 2),
    },

    [`& .${MUI_NAME}-menuItem`]: {
      fontSize: '13px',
      display: 'flex',
      alignItems: 'center',
      gap: theme.spacing(1),
      '&:hover': {
        backgroundColor: TreeColors.dark.border,
      },
      '& .MuiSvgIcon-root': {
        color: TreeColors.dark.text,
        fontSize: '16px',
      },
    },

    [`& .${MUI_NAME}-menuItemActive`]: {
      fontSize: '13px',
      display: 'flex',
      alignItems: 'center',
      gap: theme.spacing(1),
      backgroundColor: TreeColors.semantic.active,
      '&:hover': {
        backgroundColor: TreeColors.dark.border,
      },
      '& .MuiSvgIcon-root': {
        color: TreeColors.dark.text,
        fontSize: '16px',
      },
    },

    [`& .${MUI_NAME}-menuItemDelete`]: {
      fontSize: '13px',
      color: TreeColors.semantic.danger,
      display: 'flex',
      alignItems: 'center',
      gap: '8px',
      '&:hover': {
        backgroundColor: TreeColors.dark.border,
      },
      '& .MuiSvgIcon-root': {
        color: TreeColors.semantic.danger,
        fontSize: '16px',
      },
    },

    [`& .${MUI_NAME}-menuItemLocked`]: {
      fontSize: '13px',
      color: TreeColors.semantic.warning,
      display: 'flex',
      alignItems: 'center',
      gap: theme.spacing(1),
      '&:hover': {
        backgroundColor: TreeColors.dark.border,
      },
      '& .MuiSvgIcon-root': {
        color: TreeColors.semantic.warning,
        fontSize: '16px',
      },
    },

    [`& .${MUI_NAME}-menuItemUnlocked`]: {
      fontSize: '13px',
      display: 'flex',
      alignItems: 'center',
      gap: theme.spacing(1),
      '&:hover': {
        backgroundColor: TreeColors.dark.border,
      },
      '& .MuiSvgIcon-root': {
        color: TreeColors.dark.text,
        fontSize: '16px',
      },
    },

    [`& .${MUI_NAME}-divider`]: {
      borderColor: TreeColors.dark.border,
      margin: '0px !important',
    },

    [`& .${MUI_NAME}-textField`]: {
      width: '100%',
      marginTop: theme.spacing(0.5),
      '& .MuiInputBase-root': {
        backgroundColor: TreeColors.dark.background,
        color: TreeColors.dark.text,
        borderRadius: 0,
        '& fieldset': {
          borderColor: TreeColors.dark.border,
          borderRadius: 0,
        },
        '&:hover fieldset': {
          borderColor: TreeColors.light.textSecondary,
        },
        '&.Mui-focused fieldset': {
          border: `1px solid ${TreeColors.dark.text}`
        },
      },
      '& .MuiInputBase-input': {
        color: TreeColors.dark.text,
        ...theme.typography.caption,
        padding: '8px 12px',
        '&::placeholder': {
          color: TreeColors.dark.textSecondary,
          opacity: 1,
          ...theme.typography.caption,
        },
      },
      '& .MuiInputBase-input.MuiOutlinedInput-input.MuiInputBase-inputMultiline': {
        padding: 'unset',
      },
      '& .MuiInputLabel-root': {
        color: TreeColors.dark.text,
        ...theme.typography.caption,
      },
    },

    [`& .${MUI_NAME}-label`]: {
      backgroundColor: TreeColors.dark.border,
      color: TreeColors.dark.text,
      fontSize: '10px',
      height: '18px',
      border: `1px solid ${TreeColors.light.textSecondary}`,
      '& .MuiChip-label': {
        padding: '0 6px',
      },
      '& .MuiSvgIcon-root': {
        fontSize: '14px',
        color: TreeColors.semantic.warning
      }
    },

    [`& .${MUI_NAME}-expandedContent`]: {
      padding: theme.spacing(0, 2, 1, 2),
      '& .MuiTypography-body2': {
        color: TreeColors.dark.textSecondary,
        fontStyle: 'italic',
      },
    },

    [`& .${MUI_NAME}-menuContainer`]: {
      display: 'flex',
      width: '100%',
      alignItems: 'flex-start',
    },

    [`& .${MUI_NAME}-sectionMain`]: {
      width: MENU_WIDTH,
      maxHeight: MENU_HEIGHT - MENU_PADDING,
      overflowY: 'hidden',
      overflowX: 'hidden',
      display: 'flex',
      flexDirection: 'column',
      flexShrink: 0,
    },

    [`& .${MUI_NAME}-dividerSub`]: {
      borderColor: TreeColors.dark.border,
      margin: '0',
      height: '100%',
      minHeight: MENU_HEIGHT - MENU_PADDING,
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

    [`& .${MUI_NAME}-sectionSub`]: {
      width: MENU_WIDTH - MENU_PADDING,
      maxHeight: MENU_HEIGHT - MENU_PADDING,
      padding: theme.spacing(1),
      overflowY: 'auto',
      overflowX: 'hidden',
    },
  };
});