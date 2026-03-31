import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs } from '@dxs-ts/fs-api';
import { useUtilityClasses } from './useUtilityClasses';


export interface FsPropertiesLanguageProps {
  direntProps: Fs.LanguageProps;
}

export const FsPropertiesLanguage: React.FC<FsPropertiesLanguageProps> = ({ direntProps }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  return (
    <div className={classes.propertyRow}>
      <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.localeCode' })}</Typography>
      <Typography className={classes.propertyValue}>{direntProps.localeCode}</Typography>
    </div>
  );
};
