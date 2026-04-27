import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useUtilityClasses } from './useUtilityClasses';


export interface FsPropertiesDialobProps {
  dirent: Fs.DirentBase;
}

export const FsPropertiesDialob: React.FC<FsPropertiesDialobProps> = ({ dirent }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const { selectOptions } = useFsDirent();

  if (dirent.type !== 'DIALOB_FORM') {
    return undefined;
  }

  const dialobProps = dirent.props as Fs.DialobProps | undefined;
  const activeDialobTag = dialobProps ? selectOptions.getActiveDialobTag(dialobProps) : undefined;

  return (
    <>
      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.formName' })}</Typography>
        <Typography className={classes.propertyValue}>{dialobProps?.formName}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.formTechnicalId' })}</Typography>
        <Typography className={classes.propertyValue}>{dialobProps?.formTechnicalId}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.versionTag' })}</Typography>
        <Typography className={classes.propertyValue}>{activeDialobTag}</Typography>
      </div>
    </>
  );
};
