import React from 'react';
import { EveliTableHeaderRoot, useUtilityClasses } from './useUtilityClasses';


export const EveliTableHeader: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const classes = useUtilityClasses();

  return (
    <EveliTableHeaderRoot className={classes.root}>
      {children}
    </EveliTableHeaderRoot>
  )
}