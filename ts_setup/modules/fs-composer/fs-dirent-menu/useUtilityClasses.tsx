import composeClasses from '@mui/utils/composeClasses';
import { generateUtilityClass, styled, Popover, darken } from '@mui/material';
import { FsColors } from '../fs-theme';

export const MUI_NAME = 'FsDirentMenu';
export const MENU_WIDTH = 350;
export const MENU_WIDTH_EXTENDED = MENU_WIDTH * 2;
export const MENU_HEIGHT = 600;
export const MENU_PADDING = 8; // theme.spacing(0.5) * 2 for top and bottom padding

export interface FsDirentMenuClasses {
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

export type FsDirentMenuClassKey = keyof FsDirentMenuClasses;

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

export const FsDirentMenuRoot = styled(Popover, {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => !['isSubmenuOpen', 'shouldExpandUpward'].includes(prop as string),
})<{ isSubmenuOpen?: boolean; shouldExpandUpward?: boolean }>(({ theme, isSubmenuOpen }) => {
  const dangerColor = FsColors.semantic.danger;
  const warningColor = FsColors.semantic.warning;
  const activeColor = FsColors.base.border;
  const borderColor = darken(FsColors.base.border, 0.15);

  return {
    '& .MuiPaper-root': {
      backgroundColor: darken(FsColors.base.surface, 0.06),
      color: FsColors.base.text,
      border: `1px solid ${borderColor}`,
      minWidth: MENU_WIDTH,
      width: isSubmenuOpen ? MENU_WIDTH_EXTENDED : 'auto',
      height: MENU_HEIGHT,
      maxHeight: MENU_HEIGHT,
      padding: theme.spacing(0.5),
      overflow: 'hidden',
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
        backgroundColor: FsColors.base.border,
      },
      '& .MuiSvgIcon-root': {
        color: FsColors.base.text,
        fontSize: '16px',
      },
    },

    [`& .${MUI_NAME}-menuItemActive`]: {
      fontSize: '13px',
      display: 'flex',
      alignItems: 'center',
      gap: theme.spacing(1),
      backgroundColor: activeColor,
      '&:hover': {
        backgroundColor: FsColors.base.border,
      },
      '& .MuiSvgIcon-root': {
        color: FsColors.base.text,
        fontSize: '16px',
      },
    },

    [`& .${MUI_NAME}-menuItemDelete`]: {
      fontSize: '13px',
      color: dangerColor,
      display: 'flex',
      alignItems: 'center',
      gap: '8px',
      '&:hover': {
        backgroundColor: FsColors.base.border,
      },
      '& .MuiSvgIcon-root': {
        color: dangerColor,
        fontSize: '16px',
      },
    },

    [`& .${MUI_NAME}-menuItemLocked`]: {
      fontSize: '13px',
      color: warningColor,
      display: 'flex',
      alignItems: 'center',
      gap: theme.spacing(1),
      '&:hover': {
        backgroundColor: FsColors.base.border,
      },
      '& .MuiSvgIcon-root': {
        color: warningColor,
        fontSize: '16px',
      },
    },

    [`& .${MUI_NAME}-menuItemUnlocked`]: {
      fontSize: '13px',
      display: 'flex',
      alignItems: 'center',
      gap: theme.spacing(1),
      '&:hover': {
        backgroundColor: FsColors.base.border,
      },
      '& .MuiSvgIcon-root': {
        color: FsColors.base.text,
        fontSize: '16px',
      },
    },

    [`& .${MUI_NAME}-divider`]: {
      borderColor: borderColor,
      margin: '0px !important',
    },

    [`& .${MUI_NAME}-textField`]: {
      width: '100%',
      marginTop: theme.spacing(0.5),
      '& .MuiInputBase-root': {
        backgroundColor: FsColors.base.background,
        color: FsColors.base.text,
        borderRadius: 0,
        '& fieldset': {
          borderColor: borderColor,
          borderRadius: 0,
        },
        '&:hover fieldset': {
          borderColor: FsColors.base.textSecondary,
        },
        '&.Mui-focused fieldset': {
          border: `1px solid ${FsColors.base.text}`
        },
      },
      '& .MuiInputBase-input': {
        color: FsColors.base.text,
        ...theme.typography.caption,
        padding: '8px 12px',
        '&::placeholder': {
          color: FsColors.base.textSecondary,
          opacity: 1,
          ...theme.typography.caption,
        },
      },
      '& .MuiInputBase-input.MuiOutlinedInput-input.MuiInputBase-inputMultiline': {
        padding: 'unset',
      },
      '& .MuiInputLabel-root': {
        color: FsColors.base.text,
        ...theme.typography.caption,
      },
    },

    [`& .${MUI_NAME}-label`]: {
      backgroundColor: FsColors.base.border,
      color: FsColors.base.text,
      fontSize: '10px',
      height: '18px',
      border: `1px solid ${borderColor}`,
      '& .MuiChip-label': {
        padding: '0 6px',
      },
      '& .MuiSvgIcon-root': {
        fontSize: '14px',
        color: warningColor
      }
    },

    [`& .${MUI_NAME}-expandedContent`]: {
      padding: theme.spacing(0, 2, 1, 2),
      '& .MuiTypography-body2': {
        color: FsColors.base.textSecondary,
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
      borderColor: borderColor,
      margin: '0',
      height: '100%',
      minHeight: MENU_HEIGHT - MENU_PADDING,
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