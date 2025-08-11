
import { generateUtilityClass, styled, List, ListItem } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { GUserOverviewMenuProps } from './GUserOverviewMenu';

export const MUI_NAME = 'GUserOverviewMenu';

export interface GUserOverviewMenuClasses {
  root: string
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

export const GUserOverviewMenuItem = styled(ListItem, {
  name: MUI_NAME,
  slot: 'MenuItem',
  overridesResolver: (_props, styles) => {
    return [
      styles.menuItem
    ];
  },
})<{}>(({ theme }) => {
  return {
    '& .GUserOverviewMenu-overviewMenuIcon': {
      justifyContent: 'right',
      color: theme.palette.primary.main,
    },
    '& .GUserOverviewMenu-formCount': {
      backgroundColor: theme.palette.primary.main,
      color: theme.palette.primary.contrastText,
      height: '30px',
      width: '30px'
    },
    '& .GUserOverviewMenu-menuButtonLayout': {
      display: "flex",
      flexDirection: "column",
      alignItems: "flex-start"
    },
    '& .GUserOverviewMenu-userOrRepOrCompanyNameStyle': {
      ...theme.typography.caption
    },
    '& .MuiButtonBase-root.Mui-selected .GUserOverviewMenu-icon': {
      color: theme.palette.primary.contrastText,
    },
    '& .MuiButtonBase-root.Mui-selected:hover .GUserOverviewMenu-icon': {
      color: theme.palette.primary.contrastText,
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
  }
});



export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    menuItem: ['menuItem'],
    menuButton: ['menuButton'],
    menuButtonLayout: ['menuButtonLayout'],
    overviewMenuIcon: ['overviewMenuIcon'],
    userOrRepOrCompanyNameStyle: ['userOrRepOrCompanyNameStyle'],
    icon: ['icon'],
    formCount: ['formCount']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}