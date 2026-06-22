import React from 'react';
import { useIntl } from 'react-intl';

import { FsPanelErrorsProps } from './FsPanelErrorsProps';
import { useUtilityClasses } from './useUtilityClasses';


export const FsPanelErrors: React.FC<FsPanelErrorsProps> = (props) => {
  const intl = useIntl();
  const classes = useUtilityClasses();



  return <>errors</>
};

