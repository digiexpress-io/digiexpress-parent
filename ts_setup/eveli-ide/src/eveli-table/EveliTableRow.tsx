import React from 'react';
import { EveliTableRowRoot, useUtilityClasses } from './useUtilityClasses';

export const EveliTableRow: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const classes = useUtilityClasses();

  return (
    <EveliTableRowRoot className={classes.root}>{children}</EveliTableRowRoot>
  )
}