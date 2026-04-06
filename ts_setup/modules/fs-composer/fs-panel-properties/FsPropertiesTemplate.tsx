import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs } from '@dxs-ts/fs-api';
import { useUtilityClasses } from './useUtilityClasses';


export interface FsPropertiesTemplateProps {
  direntProps: Fs.TemplateProps;
}

export const FsPropertiesTemplate: React.FC<FsPropertiesTemplateProps> = ({ direntProps }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  return (
    <>
      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.printoutServiceId' })}</Typography>
        <Typography className={classes.propertyValue}>{direntProps.printoutServiceId}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.localeCode' })}</Typography>
        <Typography className={classes.propertyValue}>{direntProps.localeId}</Typography>
      </div>
    </>
  );
};
