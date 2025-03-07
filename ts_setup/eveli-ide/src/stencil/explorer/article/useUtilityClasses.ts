import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

export const ArticlesViewClassName = 'ArticlesView';
export const ArticleListClassName = 'ArticleList';
export const ArticleViewRootClassName = 'ArticleViewRoot';

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
  const getUtilityClass = (slot: string) => generateUtilityClass(ArticlesViewClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const ArticlesViewRoot = styled("div", {
  name: ArticlesViewClassName,
  slot: 'AllArticles',
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

    '& .ArticlesView-title': {
      marginLeft: theme.spacing(1),
      ...theme.typography.h1
    },

    '& .ArticlesView-searchField': {
      width: '50%',
      borderRadius: theme.spacing(3),
      alignSelf: 'center'
    },

    '& .MuiOutlinedInput-root': {
      borderRadius: theme.spacing(3),
    },

    '& .ArticlesView-searchFieldContainer': {
      display: 'flex',
      justifyContent: 'center',
      flexDirection: 'column'
    },

    '& .MuiTreeItem-root': {
     // backgroundColor: 'rgb(236, 239, 243)',
     backgroundColor: 'unset'
    },

    '& .MuiTreeItem-content': {
    },
    
    '& .MuiTreeItem-content.Mui-selected': {
      backgroundColor: 'rgb(236, 239, 243)',
      ':hover': {
        backgroundColor: 'rgb(236, 239, 243)',
      }
    }
  }
})


export const ArticlesListRoot = styled("div", {
  name: ArticleListClassName,
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