import React from 'react';

import { FlowsViewRoot } from '../wrench-explorer/flow/useUtilityClasses';
import { useUtilityClasses } from '../wrench-explorer/flow/useUtilityClasses'
import { FlowsList } from '../wrench-explorer/flow/FlowsList';



export const FlowsView: React.FC = () => {
  const classes = useUtilityClasses();


  return (
    <FlowsViewRoot className={classes.root}>
      <FlowsList  />
    </FlowsViewRoot>)
}


