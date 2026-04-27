import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs } from '@dxs-ts/fs-api';
import { useUtilityClasses } from './useUtilityClasses';


export interface FsPropertiesLanguageProps {
  dirent: Fs.Dirent;
}

export const FsPropertiesLanguage: React.FC<FsPropertiesLanguageProps> = ({ dirent }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  if (dirent.type !== 'LOCALE') {
    return undefined;
  }

  return (
    <div className={classes.propertyRow}>
      <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.localeCode' })}</Typography>
      <Typography className={classes.propertyValue}>{dirent.localeCode}</Typography>
    </div>
  );
};
