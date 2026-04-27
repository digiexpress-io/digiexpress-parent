import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useUtilityClasses } from './useUtilityClasses';


export interface FsPropertiesDialobProps {
  dirent: Fs.Dirent;
}

export const FsPropertiesDialob: React.FC<FsPropertiesDialobProps> = ({ dirent }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const { selectOptions } = useFsDirent();

  if (dirent.type !== 'DIALOB_FORM') {
    return undefined;
  }

  const activeDialobTag = selectOptions.getActiveDialobTag(dirent);

  return (
    <>
      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.formName' })}</Typography>
        <Typography className={classes.propertyValue}>{dirent.formName}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.formTechnicalId' })}</Typography>
        <Typography className={classes.propertyValue}>{dirent.formTechnicalId}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.versionTag' })}</Typography>
        <Typography className={classes.propertyValue}>{activeDialobTag}</Typography>
      </div>
    </>
  );
};
