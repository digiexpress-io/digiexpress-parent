import React from 'react';

import { ServicesViewRoot } from '../explorer/service/useUtilityClasses';
import { useUtilityClasses } from '../explorer/flow/useUtilityClasses'
import { ServicesList } from '../explorer';



export const ServicesView: React.FC = () => {
  const classes = useUtilityClasses();


  return (
    <ServicesViewRoot className={classes.root}>
      <ServicesList  />
    </ServicesViewRoot>)
}


