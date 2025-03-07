import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

export const FlowViewClassName = 'FlowView';
export const FlowsListClassName = 'FlowsList';
export const FlowsViewRootClassName = 'FlowsViewRoot';

export interface FlowsListClasses {
  root: string;
  title: string;
  searchField: string;
  searchFieldContainer: string;
}

export type FlowsListClassKey = keyof FlowsListClasses;


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    searchField: ['searchField'],
    searchFieldContainer: ['searchFieldContainer']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(FlowsViewRootClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const FlowsViewRoot = styled("div", {
  name: FlowsViewRootClassName,
  slot: 'AllFlows',
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

    '& .FlowsView-title': {
      marginLeft: theme.spacing(1),
      ...theme.typography.h1
    },

    '& .FlowsView-searchField': {
      width: '50%',
      borderRadius: theme.spacing(3),
      alignSelf: 'center'
    },

    '& .MuiOutlinedInput-root': {
      borderRadius: theme.spacing(3),
    },

    '& .FlowsView-searchFieldContainer': {
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


export const FlowsListRoot = styled("div", {
  name: FlowsListClassName,
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