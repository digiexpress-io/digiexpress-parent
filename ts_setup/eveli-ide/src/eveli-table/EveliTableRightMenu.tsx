import React from 'react';
import { generateUtilityClass, styled } from '@mui/material';

import composeClasses from '@mui/utils/composeClasses';
import { EveliTableSearchField } from './EveliTableSearchField';


export const EveliTableRightMenu: React.FC<{ width: string, children: React.ReactNode }> = ({ width, children }) => {
  const classes = useUtilityClasses();

  return (
    <EveliTableRightMenuRoot className={classes.root} width={width}>
      <EveliTableSearchField />
      {children}
    </EveliTableRightMenuRoot>
  )
}


export const EveliTableRightMenuRootClassName = 'EveliTableRightMenu';

export const EveliTableRightMenuRoot = styled('div', {
  name: EveliTableRightMenuRootClassName,
  slot: 'VerticalMenuRoot',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
    ];
  },

})<{ width: string }>(({ theme, width }) => {
  return {
    width,
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
  const getUtilityClass = (slot: string) => generateUtilityClass(EveliTableRightMenuRootClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
}
