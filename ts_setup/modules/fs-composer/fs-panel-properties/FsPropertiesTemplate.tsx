import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs } from '@dxs-ts/fs-api';
import { useUtilityClasses } from './useUtilityClasses';


export interface FsPropertiesTemplateProps {
  dirent: Fs.DirentBase;
}

export const FsPropertiesTemplate: React.FC<FsPropertiesTemplateProps> = ({ dirent }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  if (dirent.type !== 'ARTICLE_TEMPLATE') {
    return undefined;
  }

  return (
    <>
      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.printoutServiceId' })}</Typography>
        <Typography className={classes.propertyValue}>{(dirent.props as Fs.TemplateProps)?.printoutServiceId}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.localeCode' })}</Typography>
        <Typography className={classes.propertyValue}>{(dirent.props as Fs.TemplateProps)?.localeId}</Typography>
      </div>
    </>
  );
};
