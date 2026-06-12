import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { usePanelProperties } from '../fs-panel-properties';

export interface FsPropertiesPrintoutPageProps {
  direntId: string;
}

export const FsPropertiesPrintoutPage: React.FC<FsPropertiesPrintoutPageProps> = ({ direntId }) => {
  const intl = useIntl();
  const classes = usePanelProperties();
  const { selectOptions, getDirent } = useFsDirent();

  const dirent = getDirent(direntId);

  if (!dirent || dirent.type !== 'PRINTOUT_PAGE') {
    return undefined;
  }

  const pageProps = dirent.props as Fs.PrintoutPageProps;
  const printoutName = selectOptions.printouts.find(p => p.value === pageProps.serviceId)?.label ?? pageProps.serviceId;
  const localeName = selectOptions.languages.find(l => l.value === pageProps.localeId)?.label ?? pageProps.localeId;

  const connectedResources = Object.values(selectOptions.direntProps)
    .filter(p => p.type === 'PRINTOUT_RESOURCE')
    .map(p => p as Fs.PrintoutResourceProps)
    .filter(p => p.printoutPageIds.includes(pageProps.id));

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

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.printoutResources' })}</Typography>
        <div className={classes.childContainer} style={{ gap: '8px' }}>
          {connectedResources.length === 0 && (
            <Typography className={classes.propertyValue}>—</Typography>
          )}
          {connectedResources.map(resProps => (
            <div key={resProps.id} className={classes.childContainer}>
              <Typography className={classes.propertyValue}>{resProps.resourceName}</Typography>
              {resProps.contentType === 'image/*' && resProps.content && (
                <img
                  src={resProps.content}
                  alt={resProps.resourceName}
                  style={{ maxWidth: '100%', maxHeight: '200px', objectFit: 'contain' }}
                />
              )}
            </div>
          ))}
        </div>
      </div>
    </>
  );
};
