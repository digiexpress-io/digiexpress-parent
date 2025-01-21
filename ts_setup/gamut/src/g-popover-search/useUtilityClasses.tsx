import { generateUtilityClass, Popover, styled } from "@mui/material";
import composeClasses from "@mui/utils/composeClasses";
import { GPopoverSearchProps } from './GPopoverSearch';

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
}

export type GPopoverSearchClassKey = keyof GPopoverSearchClasses;

export const useUtilityClasses = (ownerState: GPopoverSearchProps) => {
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
      ...theme.typography.h1,
      marginRight: theme.spacing(1),
      textAlign: 'center'
    },
    '& .GPopoverSearch-titleContainer': {
      display: 'flex',
      alignItems: 'center',
      gap: 3
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
      paddingBottom: theme.spacing(2)
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
      minWidth: '100%',
      left: '0px !important',
      borderRadius: 'unset',
      padding: theme.spacing(1),
      maxHeight: '60vh',
      overflowY: 'auto',
      transform: 'translateY(0)'
    },
    '& .MuiLink-root': {
      display: 'block',
      textDecoration: 'none',
      marginTop: theme.spacing(1),
      marginBottom: theme.spacing(1),
      color: theme.palette.primary.dark,
      fontWeight: theme.typography.fontWeightMedium,
      '&:focus, &:hover, &:visited, &:link, &:active': {
        textDecoration: 'underline'
      }
    }
  };
});




