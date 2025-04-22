import React from 'react';
import { generateUtilityClass, styled } from '@mui/material';

import composeClasses from '@mui/utils/composeClasses';
import { Column } from '@tanstack/react-table';

import { EveliTableSearchFilter } from './EveliTableSearchFilter';
import { TaskApi } from '@/api-task';


interface EveliTableDrawerProps {
  filterItems: string[];
  column: Column<TaskApi.Task, unknown>;
  children: React.ReactNode;
}


export const EveliTableDrawer: React.FC<EveliTableDrawerProps> = ({ children, column }) => {
  const classes = useUtilityClasses();

  return (
    <EveliTableDrawerRoot className={classes.root}>
      <EveliTableSearchFilter column={column} />
      {children}
    </EveliTableDrawerRoot>
  )
}


export const EveliTableDrawerRootClassName = 'EveliTableDrawer';

export const EveliTableDrawerRoot = styled('div', {
  name: EveliTableDrawerRootClassName,
  slot: 'VerticalMenuRoot',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
    ];
  },

})(({ theme }) => {
  return {
    width: '15%',
    position: 'absolute',
    overflow: 'scroll',
    top: 0,
    bottom: 0,
    boxShadow: '-2px 0px 8px rgba(0, 0, 0, 0.1)',
    right: '3%',
    backgroundColor: theme.palette.secondary.main,
    border: `1px solid ${theme.palette.divider}`,
    zIndex: 10
  }
});

const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(EveliTableDrawerRootClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
}
