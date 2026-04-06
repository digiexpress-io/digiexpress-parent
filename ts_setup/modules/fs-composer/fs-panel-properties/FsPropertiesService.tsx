import React from 'react';
import { Typography, Box } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs } from '@dxs-ts/fs-api';
import { useUtilityClasses } from './useUtilityClasses';


export interface FsPropertiesServiceProps {
  direntProps: Fs.ServiceProps;
}

export const FsPropertiesService: React.FC<FsPropertiesServiceProps> = ({ direntProps }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const locales = Object.keys(direntProps.intlValues);

  return (
    <>
      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.serviceName' })}</Typography>
        <Typography className={classes.propertyValue}>{direntProps.serviceName}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.serviceLocales' })}</Typography>
        <div className={classes.propertyList}>
          {locales.map((locale, index) => <Box key={index} className={classes.propertyListItem}>{locale}</Box>)}
        </div>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.serviceValidityStart' })}</Typography>
        <Typography className={classes.propertyValue}>{direntProps.validityStart}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.serviceValidityEnd' })}</Typography>
        <Typography className={classes.propertyValue}>{direntProps.validityEnd}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.dialobFormName' })}</Typography>
        <Typography className={classes.propertyValue}>{direntProps.dialobFormName}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.dialobFormTag' })}</Typography>
        <Typography className={classes.propertyValue}>{direntProps.dialobFormTag}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.flowName' })}</Typography>
        <Typography className={classes.propertyValue}>{direntProps.flowName}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.selectedArticles' })}</Typography>
        <div className={classes.propertyList}>
          {direntProps.articles.map((article, index) => <Box key={index} className={classes.propertyListItem}>{article}</Box>)}
        </div>
      </div>
    </>
  );
};
