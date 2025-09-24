import React from 'react';
import { EveliActivitiesRoot, useUtilityClasses } from './useUtilityClasses';


export interface EveliActivitiesProps {
  children: React.ReactNode
}

export const EveliActivities: React.FC<EveliActivitiesProps> = ({ children }) => {
  const classes = useUtilityClasses();

  return (
    <EveliActivitiesRoot className={classes.root}>
      {children}
    </EveliActivitiesRoot>
  )
}