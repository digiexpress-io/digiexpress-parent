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
      ...theme.typography.h4,
      marginRight: theme.spacing(1),
      textAlign: 'right'
    },
    '& .GPopoverSearch-titleContainer': {
      display: 'flex',
      alignItems: 'center',
      gap: 3
    },
    '& .GPopoverSearch-layoutContainer': {
      padding: theme.spacing(3)
    },
    '& .GPopoverSearch-quickSearch': {
      marginTop: theme.spacing(2)
    },
    '& .GPopoverSearch-quickSearchFilterItem': {
      marginLeft: theme.spacing(0.5),
      marginRight: theme.spacing(0.5)
    },
    '& .GPopoverSearch-resultsContainer': {
      paddingTop: theme.spacing(2),
      paddingBottom: theme.spacing(2)
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
    '& .MuiDivider-root': {
      margin: theme.spacing(1)
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




