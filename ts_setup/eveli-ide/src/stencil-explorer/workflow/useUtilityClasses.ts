import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

export const WorkflowsViewClassName = 'WorkflowsView';
export const WorkflowsListClassName = 'WorkflowsList';
export const WorkflowsViewRootClassName = 'WorkflowsViewRoot';

export interface WorkflowListClasses {
  root: string;
  title: string;
  searchField: string;
  searchFieldContainer: string;
}

export type WorkflowListClassKey = keyof WorkflowListClasses;


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    searchField: ['searchField'],
    searchFieldContainer: ['searchFieldContainer']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(WorkflowsViewClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const WorkflowsViewRoot = styled("div", {
  name: WorkflowsViewClassName,
  slot: 'AllWorkflows',
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

    '& .WorkflowsView-title': {
      marginLeft: theme.spacing(1),
      ...theme.typography.h1
    },

    '& .WorkflowsView-searchField': {
      width: '50%',
      borderRadius: theme.spacing(3),
      alignSelf: 'center'
    },

    '& .MuiOutlinedInput-root': {
      borderRadius: theme.spacing(3),
    },

    '& .WorkflowsView-searchFieldContainer': {
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


export const WorkflowsListRoot = styled("div", {
  name: WorkflowsListClassName,
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