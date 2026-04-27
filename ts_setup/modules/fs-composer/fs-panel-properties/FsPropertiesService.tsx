import React from 'react';
import { Typography, Box } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs } from '@dxs-ts/fs-api';
import { useUtilityClasses } from './useUtilityClasses';


export interface FsPropertiesServiceProps {
  dirent: Fs.DirentBase;
}

export const FsPropertiesService: React.FC<FsPropertiesServiceProps> = ({ dirent }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  if (dirent.type !== 'ARTICLE_WORKFLOW') {
    return undefined;
  }
  const serviceProps = dirent.props as Fs.ServiceProps | undefined;
  const locales = Object.keys(serviceProps?.intlValues ?? {});

  return (
    <>
      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.serviceName' })}</Typography>
        <Typography className={classes.propertyValue}>{serviceProps?.serviceName}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.serviceLocales' })}</Typography>
        <div className={classes.propertyList}>
          {locales.map((locale, index) => <Box key={index} className={classes.propertyListItem}>{locale}</Box>)}
        </div>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.serviceValidityStart' })}</Typography>
        <Typography className={classes.propertyValue}>{serviceProps?.validityStart}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.serviceValidityEnd' })}</Typography>
        <Typography className={classes.propertyValue}>{serviceProps?.validityEnd}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.dialobFormName' })}</Typography>
        <Typography className={classes.propertyValue}>{serviceProps?.dialobFormName}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.dialobFormTag' })}</Typography>
        <Typography className={classes.propertyValue}>{serviceProps?.dialobFormTag}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.flowName' })}</Typography>
        <Typography className={classes.propertyValue}>{serviceProps?.flowName}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.selectedArticles' })}</Typography>
        <div className={classes.propertyList}>
          {(serviceProps?.articles ?? []).map((article, index) => <Box key={index} className={classes.propertyListItem}>{article}</Box>)}
        </div>
      </div>
    </>
  );
};
