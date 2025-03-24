import React from 'react';

import { DecisionsViewRoot } from '../wrench-explorer/decision/useUtilityClasses';
import { useUtilityClasses } from '../wrench-explorer/flow/useUtilityClasses'
import { DecisionsList } from '../wrench-explorer/decision/DecisionsList';


export const DecisionsView: React.FC = () => {
  const classes = useUtilityClasses();


  return (
    <DecisionsViewRoot className={classes.root}>
      <DecisionsList  />
    </DecisionsViewRoot>)
}


