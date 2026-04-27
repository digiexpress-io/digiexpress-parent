import React from 'react';
import { Typography, Box } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs } from '@dxs-ts/fs-api';
import { useUtilityClasses } from './useUtilityClasses';


export interface FsPropertiesServiceProps {
  dirent: Fs.Dirent;
}

export const FsPropertiesService: React.FC<FsPropertiesServiceProps> = ({ dirent }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  if (dirent.type !== 'ARTICLE_WORKFLOW') {
    return undefined;
  }
  const locales = Object.keys(dirent.intlValues ?? {});

  return (
    <>
      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.serviceName' })}</Typography>
        <Typography className={classes.propertyValue}>{dirent.serviceName}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.serviceLocales' })}</Typography>
        <div className={classes.propertyList}>
          {locales.map((locale, index) => <Box key={index} className={classes.propertyListItem}>{locale}</Box>)}
        </div>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.serviceValidityStart' })}</Typography>
        <Typography className={classes.propertyValue}>{dirent.validityStart}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.serviceValidityEnd' })}</Typography>
        <Typography className={classes.propertyValue}>{dirent.validityEnd}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.dialobFormName' })}</Typography>
        <Typography className={classes.propertyValue}>{dirent.dialobFormName}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.dialobFormTag' })}</Typography>
        <Typography className={classes.propertyValue}>{dirent.dialobFormTag}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.flowName' })}</Typography>
        <Typography className={classes.propertyValue}>{dirent.flowName}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.selectedArticles' })}</Typography>
        <div className={classes.propertyList}>
          {dirent.articles.map((article, index) => <Box key={index} className={classes.propertyListItem}>{article}</Box>)}
        </div>
      </div>
    </>
  );
};
