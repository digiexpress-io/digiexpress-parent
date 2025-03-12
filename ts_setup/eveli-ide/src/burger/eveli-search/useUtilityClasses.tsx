
import { generateUtilityClass, Popover, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { EveliSearchProps } from './EveliSearch';

export const MUI_NAME = 'EveliSearch';
export interface EveliSearchClasses {
  root: string;
  searchFieldContainer: string;
  searchField: string;
  searchFieldContainerTitle: string;
  searchFilterActive: string;
  searchFilter: string;
}

export type EveliSearchClassKey = keyof EveliSearchClasses;


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    searchField: ['searchField'],
    searchFieldContainer: ['searchFieldContainer'],
    searchFieldContainerTitle: ['searchFieldContainerTitle'],
    searchFilterActive: ['searchFilterActive'],
    searchFilter: ['searchFilter']

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}



export const EveliSearchRoot = styled("div", {
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
})<{ ownerState: EveliSearchProps }>(({ theme }) => {
  return {
    padding: theme.spacing(1),
    width: '100%',

    '& .EveliSearch-searchField': {
      width: '50%',
      borderRadius: theme.spacing(3),
      alignSelf: 'center'
    },

    '& .MuiOutlinedInput-root': {
      borderRadius: theme.spacing(3),
    },

    '& .EveliSearch-searchFieldContainer': {
      display: 'flex',
      justifyContent: 'center',
      flexDirection: 'column'
    },

    '& .EveliSearch-searchFieldContainerTitle': {
      textAlign: 'center',
      ...theme.typography.h1
    },

    '& .EveliSearch-searchFilterActive': {
      minWidth: '8ch',
      border: `1px solid #CED8DE`,
    },

    '& .EveliSearch-searchFilter': {
      minWidth: '8ch',
      backgroundColor: 'white',
      border: `1px solid white`,
    },

  }
})