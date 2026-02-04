import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

export const ImagesViewClassName = 'ImagesView';
export const ImagesListClassName = 'ImagesList';
export const ImagesViewRootClassName = 'ImagesViewRoot';

export interface ImagesViewClasses {
  root: string;
  title: string;
}

export type ImagesViewClassKey = keyof ImagesViewClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
  };
  const getUtilityClass = (slot: string) =>
    generateUtilityClass(ImagesViewClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const ImagesViewRoot = styled('div', {
  name: ImagesViewClassName,
  slot: 'AllImages',
  overridesResolver: (props, styles) => {
    return [styles.root, styles.title];
  },
})<{}>(({ theme }) => {
  return {
    '& .ImagesView-title': {
      marginLeft: theme.spacing(1),
      ...theme.typography.h1,
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
      },
    },
    '& .MuiTreeItem-content:hover': {
      borderRadius: 'unset',
    },
  };
});

export const ImagesListRoot = styled('div', {
  name: ImagesListClassName,
  slot: 'TreeItems',
  overridesResolver: (_props, styles) => {
    return [styles.root];
  },
})<{}>(() => {
  return {};
});
