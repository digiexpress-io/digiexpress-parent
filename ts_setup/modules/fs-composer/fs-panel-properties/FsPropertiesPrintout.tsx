import React from 'react';
import { Typography, Box } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs } from '@dxs-ts/fs-api';
import { useUtilityClasses } from './useUtilityClasses';


export interface FsPropertiesPrintoutProps {
  direntProps: Fs.PrintoutProps;
}

export const FsPropertiesPrintout: React.FC<FsPropertiesPrintoutProps> = ({ direntProps }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const locales = Object.keys(direntProps.intlValues);

  return (
    <>
      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.printoutServiceName' })}</Typography>
        <Typography className={classes.propertyValue}>{direntProps.printoutServiceName}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.orchestratorName' })}</Typography>
        <Typography className={classes.propertyValue}>{direntProps.orchestratorName}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyListItem}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.locales' })}</Typography>
        <div className={classes.propertyList}>
          {locales.map((locale, index) => <Box key={index} className={classes.propertyListItem}>{locale}</Box>)}
        </div>
      </div>
    </>
  );
};
