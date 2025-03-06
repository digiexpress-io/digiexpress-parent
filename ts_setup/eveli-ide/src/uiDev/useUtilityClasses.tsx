
import { generateUtilityClass, Popover, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { UiDevAppProps } from './UiDev'
import { UiDevSearchProps } from './UiDevSearchGeneral';

export const MUI_NAME = 'UiDevApp';
export interface UiDevAppClasses {
  root: string;
  explorerContainer: string;
  explorerToolbar: string;
  composeButton: string;
  toolbarIcon: string;
  toolbarIconText: string;
  menuButton: string;
  menuButtonActive: string;
  logoutButton: string;
  explorerDivider: string;
  logoContainer: string;
  logo: string;
  menuListItem: string;
  popoverTitle: string;
  searchFieldContainer: string;
  searchField: string;
  searchFieldContainerTitle: string;
  searchFilterActive: string;
  searchFilter: string;
}

export type UiDevAppClassKey = keyof UiDevAppClasses;


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    explorerContainer: ['explorerContainer'],
    explorerToolbar: ['explorerToolbar'],
    composeButton: ['composeButton'],
    toolbarIcon: ['toolbarIcon'],
    toolbarIconText: ['toolbarIconText'],
    menuButton: ['menuButton'],
    menuButtonActive: ['menuButtonActive'],
    logoutButton: ['logoutButton'],
    explorerDivider: ['explorerDivider'],
    logoContainer: ['logoContainer'],
    logo: ['logo'],
    menuListItem: ['menuListItem'],
    popoverTitle: ['popoverTitle'],
    searchField: ['searchField'],
    searchFieldContainer: ['searchFieldContainer'],
    searchFieldContainerTitle: ['searchFieldContainerTitle'],
    searchFilterActive: ['searchFilterActive'],
    searchFilter: ['searchFilter']

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const UiDevAppRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      styles.explorerContainer,
      styles.explorerToolbar,
      styles.composeButton,
      styles.toolbarIcon,
      styles.toolbarIconText,
      styles.menuButton,
      styles.menuButtonActive,
      styles.logoutButton,
      styles.explorerDivider,
      styles.logoContainer,
      styles.logo,
      styles.mmenuListItem,
      styles.popoverTitle,
      styles.searchField,
      styles.searchFieldContainer,
      styles.searchFieldContainerTitle,
      styles.searchFilterActive,
      styles.searchFilter
    ];
  },
})<{ ownerState: UiDevAppProps }>(({ theme }) => {
  return {
    display: 'flex',
    marginTop: 'auto',
    height: '100vh',

    '& .UiDevApp-explorerToolbar': {
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      gap: theme.spacing(2),
      backgroundColor: 'rgb(236, 239, 243)',
      width: '50px',
      height: '100%',
      borderRight: `1px solid #CED8DE`,
      paddingLeft: theme.spacing(1),
      paddingRight: theme.spacing(1),
      paddingTop: theme.spacing(1),
    },

    '& .UiDevApp-explorerContainer': {
      paddingLeft: theme.spacing(1),
      paddingRight: theme.spacing(1),
      paddingTop: theme.spacing(1),
      backgroundColor: 'rgb(246, 249, 253)',
      width: '250px',
      height: '100%',
      borderRight: `1px solid #CED8DE`
    },

    '& .UiDevApp-composeButton': {
      backgroundColor: '#FFFFFF',
      borderRadius: theme.spacing(2),
      color: 'rgb(58, 55, 55)',
      width: '100%',
      ...theme.typography.body1,
      fontWeight: 'bold',
      padding: theme.spacing(2),
      marginBottom: theme.spacing(3),
      ':hover': {
        backgroundColor: '#FFFFFF'
      },
    },

    '& .UiDevApp-menuButton': {
      justifyContent: 'left',
      marginTop: theme.spacing(0.5),
      borderRadius: theme.spacing(3),
      paddingLeft: theme.spacing(2),
      border: `1px solid rgb(246, 249, 253)`,
      ...theme.typography.body1,
      color: 'rgb(58, 55, 55)',
      width: '100%',
      ':hover': {
        backgroundColor: 'rgb(236, 239, 243)',
        border: `1px solid rgb(246, 249, 253)`,
      }
    },

    '& .UiDevApp-menuButtonActive': {
      justifyContent: 'left',
      marginTop: theme.spacing(0.5),
      borderRadius: theme.spacing(3),
      border: `1px solid #CED8DE`,
      ...theme.typography.body1,
      paddingLeft: theme.spacing(2),
      fontWeight: 'bold',
      color: 'rgb(58, 55, 55)',
      width: '100%',
      backgroundColor: 'rgb(236, 239, 243)',
      ':hover': {
        backgroundColor: 'rgb(236, 239, 243)',
        border: `1px solid #CED8DE`,
      }
    },

    '& .UiDevApp-logoutButton': {
      justifyContent: 'left',
      alignItems: "flex-start",
      marginTop: theme.spacing(0.5),
      borderRadius: theme.spacing(3),
      paddingLeft: theme.spacing(2),
      border: `1px solid rgb(246, 249, 253)`,
      ...theme.typography.body1,
      color: 'rgb(58, 55, 55)',
      width: '100%',
      ':hover': {
        backgroundColor: 'rgb(236, 239, 243)',
        border: `1px solid rgb(246, 249, 253)`,
      }
    },

    '& .UiDevApp-toolbarIcon': {
      color: 'rgb(58, 55, 55)',
    },

    '& .UiDevApp-toolbarIconText': {
      ...theme.typography.caption
    },

    '& .UiDevApp-explorerDivider': {
      borderWidth: `1px solid #CED8DE`,
      marginTop: theme.spacing(1),
      marginBottom: theme.spacing(1)
    },

    '& .UiDevApp-logoContainer': {
      display: 'flex',
      justifyContent: 'center'
    },

    '& .UiDevApp-logo': {
      height: '45px',
      width: '160px',
      marginBottom: theme.spacing(2)
    }

  };
});


export const UiDevAppPopoverRoot = styled(Popover, {
  name: MUI_NAME,
  slot: 'Popover',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      styles.popoverTitle
    ];
  },
})(({ theme }) => {
  return {
    '& .MuiPaper-root': {
      minWidth: 200
    },
    '& .UiDevApp-popoverTitle': {
      fontWeight: 'bold',
      padding: theme.spacing(2)
    }
  }
})

export const UiDevAppSearchRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Search',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      styles.searchField,
      styles.searchFieldContainerTitle,
      styles.searchFilterActive,
      styles.searchFilter
    ];
  },
})<{ ownerState: UiDevSearchProps }>(({ theme }) => {
  return {
    padding: theme.spacing(1),
    width: '100%',

    '& .UiDevApp-searchField': {
      width: '50%',
      borderRadius: theme.spacing(3),
      alignSelf: 'center'
    },

    '& .MuiOutlinedInput-root': {
      borderRadius: theme.spacing(3),
    },

    '& .UiDevApp-searchFieldContainer': {
      display: 'flex',
      justifyContent: 'center',
      flexDirection: 'column'
    },

    '& .UiDevApp-searchFieldContainerTitle': {
      textAlign: 'center',
      ...theme.typography.h1
    },

    '& .UiDevApp-searchFilterActive': {
      minWidth: '8ch',
      border: `1px solid #CED8DE`,
    },

    '& .UiDevApp-searchFilter': {
      minWidth: '8ch',
      backgroundColor: 'white',
      border: `1px solid white`,
    },

  }
})