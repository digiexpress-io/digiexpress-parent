import React from 'react';
import { Typography, Box } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirent } from '@dxs-ts/fs-api';
import { useUtilityClasses } from './useUtilityClasses';


export interface FsPropertiesPhoneProps {
  direntProps: FsDirent.PhoneProps;
}

export const FsPropertiesPhone: React.FC<FsPropertiesPhoneProps> = ({ direntProps }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const locales = Object.keys(direntProps.intlValues);

  return (
    <>
      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.phoneValue' })}</Typography>
        <Typography className={classes.propertyValue}>{direntProps.phoneValue}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.locales' })}</Typography>
        <div className={classes.propertyList}>
          {locales.map((locale, index) => <Box key={index} className={classes.propertyListItem}>{locale}</Box>)}
        </div>
      </div>
    </>
  );
};
