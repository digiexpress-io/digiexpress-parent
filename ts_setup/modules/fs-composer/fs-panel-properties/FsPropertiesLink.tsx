import React from 'react';
import { Typography, Box } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useUtilityClasses } from './useUtilityClasses';


export interface FsPropertiesLinkProps {
  dirent: Fs.DirentBase;
}

export const FsPropertiesLink: React.FC<FsPropertiesLinkProps> = ({ dirent }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const { selectOptions } = useFsDirent();

  if (dirent.type !== 'ARTICLE_LINK') {
    return undefined;
  }

  const intlValues = (dirent.props as Fs.LinkProps).intlValues;
  const locales = Object.keys(intlValues).map((localeId) => {
    const lang = selectOptions.languages.find((l) => l.value === localeId);
    return lang?.label ?? localeId;
  })

  return (
    <>
      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.urlValue' })}</Typography>
        <Typography className={classes.propertyValue}>{dirent.name}</Typography>
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


