import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

export const ScriptsViewClassName = 'ScriptsView';
export const ScriptsListClassName = 'ScriptsList';
export const ScriptsViewRootClassName = 'ScriptsViewRoot';

export interface ScriptsViewClasses {
  root: string;
  title: string;
}

export type ScriptsViewClassKey = keyof ScriptsViewClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
  };
  const getUtilityClass = (slot: string) =>
    generateUtilityClass(ScriptsViewClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const ScriptsViewRoot = styled('div', {
  name: ScriptsViewClassName,
  slot: 'AllScripts',
  overridesResolver: (props, styles) => {
    return [styles.root, styles.title];
  },
})<{}>(({ theme }) => {
  return {
    '& .ScriptsView-title': {
      marginLeft: theme.spacing(1),
      ...theme.typography.h1,
    },
    '& .MuiOutlinedInput-root': {
      borderRadius: theme.spacing(3),
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

export const ScriptsListRoot = styled('div', {
  name: ScriptsListClassName,
  slot: 'TreeItems',
  overridesResolver: (_props, styles) => {
    return [styles.root];
  },
})<{}>(({ theme }) => {
  return {};
});
