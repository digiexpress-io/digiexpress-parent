import React from 'react';

import { ServicesViewRoot } from '../wrench-explorer/service/useUtilityClasses';
import { useUtilityClasses } from '../wrench-explorer/flow/useUtilityClasses'
import { ServicesList } from '../wrench-explorer';



export const ServicesView: React.FC = () => {
  const classes = useUtilityClasses();


  return (
    <ServicesViewRoot className={classes.root}>
      <ServicesList  />
    </ServicesViewRoot>)
}


