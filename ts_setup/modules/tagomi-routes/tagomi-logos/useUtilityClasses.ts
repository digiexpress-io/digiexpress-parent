import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

export const LogosViewClassName = 'LogosView';
export const LogosListClassName = 'LogosList';
export const LogosViewRootClassName = 'LogosViewRoot';

export interface LogosViewClasses {
  root: string;
  title: string;
  searchField: string;
  searchFieldContainer: string;
}

export type LogosViewClassKey = keyof LogosViewClasses;


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    searchField: ['searchField'],
    searchFieldContainer: ['searchFieldContainer']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(LogosViewClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const LogosViewRoot = styled("div", {
  name: LogosViewClassName,
  slot: 'AllLogos',
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

    '& .LogosView-title': {
      marginLeft: theme.spacing(1),
      ...theme.typography.h1
    },

    '& .LogosView-searchField': {
      width: '50%',
      borderRadius: theme.spacing(3),
      alignSelf: 'center'
    },

    '& .MuiOutlinedInput-root': {
      borderRadius: theme.spacing(3),
    },

    '& .LogosView-searchFieldContainer': {
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


export const LogosListRoot = styled("div", {
  name: LogosListClassName,
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
