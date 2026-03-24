import React from 'react';
import { Typography, Box } from '@mui/material';
import { useIntl } from 'react-intl';
import { LinkDirentProps } from '@dxs-ts/fs-api';
import { useUtilityClasses } from './useUtilityClasses';


export interface FsPropertiesLinkProps {
  direntProps: LinkDirentProps;
}

export const FsPropertiesLink: React.FC<FsPropertiesLinkProps> = ({ direntProps }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const locales = Object.keys(direntProps.intlValues);

  return (
    <>
      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.urlValue' })}</Typography>
        <Typography className={classes.propertyValue}>{direntProps.urlValue}</Typography>
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
