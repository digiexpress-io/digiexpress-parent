import { alpha, generateUtilityClass, lighten, Popover, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";


export const MUI_NAME = 'GPopoverSearch';

export interface GPopoverSearchClasses {
  root: string;
  title: string;
  titleContainer: string;

  layoutContainer: string;

  quickSearch: string;
  quickSearchFilterItem: string;

  resultsContainer: string;
  resultsDividerTitle: string;
  inputField: string;
  inputFieldContainer: string;

  logoBox: string;
  childTopic: string;
}

export type GPopoverSearchClassKey = keyof GPopoverSearchClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    titleContainer: ['titleContainer'],
    layoutContainer: ['layoutContainer'],
    quickSearch: ['quickSearch'],
    quickSearchFilterItem: ['quickSearchFilterItem'],
    resultsContainer: ['resultsContainer'],
    inputField: ['inputField'],
    inputFieldContainer: ['inputFieldContainer'],
    resultsDividerTitle: ['resultsDividerTitle'],
    logoBox: ['logoBox'],
    childTopic: ['childTopic']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}

export const GPopoverSearchRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.topics,
    ];
  },
})(({ theme }) => {
  return {

  };
});


export const GSearchMuiPopover = styled(Popover, {
  name: MUI_NAME,
  slot: 'Search',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.title,
      styles.titleContainer,
      styles.layoutContainer,
      styles.quickSearch,
      styles.quickSearchFilterItem,
      styles.resultsContainer,
      styles.inputField,
      styles.inputFieldContainer,
      styles.resultsDividerTitle,
      styles.logoBox
    ];
  },
})(({ theme }) => {
  return {
    '& .GPopoverSearch-inputFieldContainer': {
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
    },
    '& .GPopoverSearch-inputField': {
      width: '70ch',
      backgroundColor: theme.palette.primary.contrastText,
    },
    '& .GPopoverSearch-title': {
      ...theme.typography.body1,
      fontWeight: 'bold',
      marginRight: theme.spacing(1),
    },
    '& .GPopoverSearch-titleContainer': {
      display: 'flex',
      justifyContent: 'center',
      gap: 3,
      [theme.breakpoints.up('md')]: {
        flexDirection: 'row',
        alignItems: 'center',
      },
      [theme.breakpoints.down('md')]: {
        flexDirection: 'column',
        alignItems: 'flex-start',
      },
    },    
    '& .GPopoverSearch-logoBox': {
      [theme.breakpoints.up('sm')]: {
        display: 'none'
      },
      display: 'flex',
      alignItems: 'center',
      marginBottom: theme.spacing(3),
      justifyContent: 'space-between'
    },
    '& .GPopoverSearch-layoutContainer': {
      [theme.breakpoints.up('md')]: {
        padding: theme.spacing(3)
      },
      [theme.breakpoints.down('md')]: {
        padding: theme.spacing(1)
      }
    },
    '& .GPopoverSearch-quickSearch': {
      [theme.breakpoints.up('md')]: {
        paddingTop: theme.spacing(2),
        textAlignLast: 'center',
      },

      [theme.breakpoints.down('md')]: {
        gap: theme.spacing(1),
        marginTop: theme.spacing(2),
        display: 'flex',
        flexDirection: 'column'
      },

    },
    '& .GPopoverSearch-quickSearchFilterItem': {
      marginLeft: theme.spacing(0.5),
      marginRight: theme.spacing(0.5)
    },
    '& .GPopoverSearch-resultsContainer': {
      paddingTop: theme.spacing(2),
      paddingBottom: theme.spacing(2),
      marginLeft: 'auto',
      marginRight: 'auto',
      width: '100%',
      [theme.breakpoints.up('md')]: {
        maxWidth: '1000px',
      },
    },
    '& .GPopoverSearch-resultsDividerTitle': {
      ...theme.typography.h1,
      [theme.breakpoints.down('md')]: {
        textAlign: 'center',
        marginBottom: theme.spacing(1)
      },

      [theme.breakpoints.up('md')]: {
        ...theme.typography.h3,
        textAlign: 'left',
        marginBottom: theme.spacing(1)
      },
    },

    '& .GPopoverSearch-resultsDividerTitle.MuiDivider-root': {
      marginTop: theme.spacing(2),
      border: `1px solid ${theme.palette.primary.main}`
    },

    '& .MuiPopover-paper': {
      left: '50% !important',
      transform: 'translateX(-50%) !important',
      minWidth: '60%',
      margin: '0 auto',
      borderRadius: 'unset',
      paddingTop: theme.spacing(3),
      maxHeight: '60vh',
      overflowY: 'auto',
      borderColor: theme.palette.divider,
      boxShadow: '0px 7px 15px 0px rgba(194,190,194,0.7)',
    
      [theme.breakpoints.down('md')]: {
        left: '0px !important',
        transform: 'none !important',
        width: '100%',
        maxWidth: '100%',
        boxSizing: 'border-box',
        minHeight: '100vh',
        padding: theme.spacing(2),
        overflow: 'auto',
        top: '0px !important',
      },
    },    
    
    '& .MuiLink-root': {
      display: 'block',
      textDecoration: 'none',
      marginTop: theme.spacing(1),
      marginBottom: theme.spacing(1),
      fontWeight: 'bold',
      color: theme.palette.text.primary,
      '&:focus, &:hover, &:visited, &:link, &:active': {
        textDecoration: 'underline'
      }
    },

    '& .GPopoverSearch-childTopic': {
      display: 'flex',
      alignItems: 'center',
      ...theme.typography.body2,
      color: theme.palette.text.secondary,
      marginLeft: theme.spacing(1),
      marginTop: 0,
      marginBottom: 0,
    },
    
    '& .GPopoverSearch-childTopic .MuiSvgIcon-root': {
      fontSize: '6pt',
      marginRight: theme.spacing(1),
      color: theme.palette.primary.main
    },
    
  };
});




