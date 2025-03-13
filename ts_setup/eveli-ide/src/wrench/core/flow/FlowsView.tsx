import React from 'react';

import { FlowsViewRoot } from '../explorer/flow/useUtilityClasses';
import { useUtilityClasses } from '../explorer/flow/useUtilityClasses'
import { FlowsList } from '../explorer/flow/FlowsList';



export const FlowsView: React.FC = () => {
  const classes = useUtilityClasses();


  return (
    <FlowsViewRoot className={classes.root}>
      <FlowsList  />
    </FlowsViewRoot>)
}


