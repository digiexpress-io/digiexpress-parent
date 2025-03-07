import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

export const DecisionsViewClassName = 'DecisionsView';
export const DecisionsListClassName = 'DecisionsList';
export const DecisionsViewRootClassName = 'DecisionsViewRoot';

export interface DecisionsListClasses {
  root: string;
  title: string;
  searchField: string;
  searchFieldContainer: string;
}

export type DecisionsListClassKey = keyof DecisionsListClasses;


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    searchField: ['searchField'],
    searchFieldContainer: ['searchFieldContainer']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(DecisionsViewRootClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const DecisionsViewRoot = styled("div", {
  name: DecisionsViewRootClassName,
  slot: 'AllDecisions',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      styles.title,
      styles.searchField,
      styles.searchFieldContainer

    ];
  },
})<{}>(({ theme }) => {
  return {

    '& .DecisionsView-title': {
      marginLeft: theme.spacing(1),
      ...theme.typography.h1
    },

    '& .DecisionsView-searchField': {
      width: '50%',
      borderRadius: theme.spacing(3),
      alignSelf: 'center'
    },

    '& .MuiOutlinedInput-root': {
      borderRadius: theme.spacing(3),
    },

    '& .DecisionsView-searchFieldContainer': {
      display: 'flex',
      justifyContent: 'center',
      flexDirection: 'column'
    },

    '& .MuiTreeItem-root': {
      backgroundColor: 'unset',
      color: 'rgb(58, 55, 55)',
    },

    '& .MuiTreeItem-content.Mui-selected': {
      backgroundColor: 'rgb(236, 239, 243)',
      borderRadius: 'unset',
      ':hover': {
        backgroundColor: 'rgb(236, 239, 243)',
      }
    },
    '& .MuiTreeItem-content:hover': {
      borderRadius: 'unset',
    },

  }
})


export const DecisionsListRoot = styled("div", {
  name: DecisionsListClassName,
  slot: 'TreeItems',
  overridesResolver: (props, styles) => {
    return [
      styles.root,

    ];
  },
})<{}>(({ theme }) => {
  return {

  }
})