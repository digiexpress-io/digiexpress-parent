import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

export const ServicesViewClassName = 'ServicesView';
export const ServicesListClassName = 'ServicesList';
export const ServicesViewRootClassName = 'ServicesViewRoot';

export interface ArticleListClasses {
  root: string;
  title: string;
  searchField: string;
  searchFieldContainer: string;
}

export type ArticleListClassKey = keyof ArticleListClasses;


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    searchField: ['searchField'],
    searchFieldContainer: ['searchFieldContainer']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(ServicesViewClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const ServicesViewRoot = styled("div", {
  name: ServicesViewClassName,
  slot: 'AllServices',
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

    '& .ServicesView-title': {
      marginLeft: theme.spacing(1),
      ...theme.typography.h1
    },

    '& .ServicesView-searchField': {
      width: '50%',
      borderRadius: theme.spacing(3),
      alignSelf: 'center'
    },

    '& .MuiOutlinedInput-root': {
      borderRadius: theme.spacing(3),
    },

    '& .ServicesView-searchFieldContainer': {
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


export const ServicesListRoot = styled("div", {
  name: ServicesListClassName,
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