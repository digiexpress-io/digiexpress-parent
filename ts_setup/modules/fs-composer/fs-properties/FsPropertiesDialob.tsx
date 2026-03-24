import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { DialobDirentProps } from '@dxs-ts/fs-api';
import { useUtilityClasses } from './useUtilityClasses';


export interface FsPropertiesDialobProps {
  direntProps: DialobDirentProps;
}

export const FsPropertiesDialob: React.FC<FsPropertiesDialobProps> = ({ direntProps }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  return (
    <>
      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.formName' })}</Typography>
        <Typography className={classes.propertyValue}>{direntProps.formName}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.formTechnicalId' })}</Typography>
        <Typography className={classes.propertyValue}>{direntProps.formTechnicalId}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.versionTag' })}</Typography>
        <Typography className={classes.propertyValue}>{direntProps.versionTag}</Typography>
      </div>
    </>
  );
};
