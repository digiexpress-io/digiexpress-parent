
import { generateUtilityClass, styled, List, ListItem } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { GUserOverviewMenuProps } from './GUserOverviewMenu';

export const MUI_NAME = 'GUserOverviewMenu'

export interface GUserOverviewMenuClasses {
  root: string;
  item: string;
  menuButton: string;
  icon: string;
  formCount: string;
}
export type GUserOverviewMenuClassKey = keyof GUserOverviewMenuClasses;


export const GUserOverviewMenuRoot = styled(List, {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },
})<{ ownerState: GUserOverviewMenuProps }>(({ theme }) => {
  return {
    paddingTop: theme.spacing(2),
    '& .MuiListItem-root': {
      padding: 0,
    },
  };
});

export const GUserOverviewMenuItemRoot = styled(ListItem, {
  name: MUI_NAME,
  slot: 'Item',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.item,
      styles.menuButton,
      styles.icon,
      styles.formCount
    ];
  },
})<{}>(({ theme }) => {
  return {
    '& .GUserOverviewMenu-icon': {
      justifyContent: 'right',
      color: theme.palette.primary.main,
    },
    '& .MuiButtonBase-root': {
      justifyContent: 'space-between',
      padding: theme.spacing(2),
    },
    '& .MuiButtonBase-root.Mui-selected': {
      color: theme.palette.primary.contrastText,
      backgroundColor: theme.palette.primary.main,
      ':hover': {
        backgroundColor: theme.palette.primary.light
      }
    },
    '& .GUserOverviewMenu-formCount': {
      backgroundColor: theme.palette.primary.main,
      color: theme.palette.primary.contrastText,
      height: '30px',
      width: '30px'
    }
  }
});



export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    item: ['item'],
    menuButton: ['menuButton'],
    icon: ['icon'],
    formCount: ['formCount']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}