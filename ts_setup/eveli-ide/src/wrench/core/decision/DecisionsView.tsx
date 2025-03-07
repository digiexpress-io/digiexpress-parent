import React from 'react';

import { DecisionsViewRoot } from '../explorer/decision/useUtilityClasses';
import { useUtilityClasses } from '../explorer/flow/useUtilityClasses'
import { DecisionsList } from '../explorer/decision/DecisionsList';


export const DecisionsView: React.FC = () => {
  const classes = useUtilityClasses();


  return (
    <DecisionsViewRoot className={classes.root}>
      <DecisionsList  />
    </DecisionsViewRoot>)
}


