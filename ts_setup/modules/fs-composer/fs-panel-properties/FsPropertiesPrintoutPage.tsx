import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useUtilityClasses } from './useUtilityClasses';

export interface FsPropertiesPrintoutPageProps {
  dirent: Fs.DirentBase;
}

export const FsPropertiesPrintoutPage: React.FC<FsPropertiesPrintoutPageProps> = ({ dirent }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const { selectOptions } = useFsDirent();

  if (dirent.type !== 'PRINTOUT_PAGE') {
    return undefined;
  }

  const pageProps = dirent.props as Fs.PrintoutPageProps;
  const printoutName = selectOptions.printouts.find(p => p.value === pageProps.serviceId)?.label ?? pageProps.serviceId;
  const localeName = selectOptions.languages.find(l => l.value === pageProps.localeId)?.label ?? pageProps.localeId;

  return (
    <>
      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.printoutServiceId' })}</Typography>
        <Typography className={classes.propertyValue}>{printoutName}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.localeCode' })}</Typography>
        <Typography className={classes.propertyValue}>{localeName}</Typography>
      </div>
    </>
  );
};
