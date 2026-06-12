import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { usePanelProperties } from '../fs-panel-properties';

export interface FsPropertiesPrintoutResourceProps {
  direntId: string;
}

export const FsPropertiesPrintoutResource: React.FC<FsPropertiesPrintoutResourceProps> = ({ direntId }) => {
  const intl = useIntl();
  const classes = usePanelProperties();
  const { selectOptions, getDirent } = useFsDirent();

  const dirent = getDirent(direntId);

  if (!dirent || dirent.type !== 'PRINTOUT_RESOURCE') {
    return undefined;
  }

  const resourceProps = dirent.props as Fs.PrintoutResourceProps;

  const connectedPages = resourceProps.printoutPageIds.map(pageId => {
    const pageProps = selectOptions.direntProps[pageId] as Fs.PrintoutPageProps | undefined;
    const printoutName = pageProps
      ? (selectOptions.printouts.find(p => p.value === pageProps.serviceId)?.label ?? pageProps.serviceId)
      : pageId;
    const localeName = pageProps
      ? (selectOptions.languages.find(l => l.value === pageProps.localeId)?.label ?? pageProps.localeId)
      : pageId;
    return { id: pageId, label: `${printoutName} / ${localeName}` };
  });

  return (
    <>
      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.resourceContentType' })}</Typography>
        <Typography className={classes.propertyValue}>{resourceProps.contentType}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.printoutPages' })}</Typography>
        <div className={classes.childContainer} style={{ gap: '8px' }}>
          {connectedPages.length === 0 && (
            <Typography className={classes.propertyValue}>—</Typography>
          )}
          {connectedPages.map(page => (
            <Typography key={page.id} className={classes.propertyValue}>{page.label}</Typography>
          ))}
        </div>
      </div>

      {resourceProps.contentType === 'image/*' && resourceProps.content && (
        <div className={classes.propertyRow}>
          <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.preview' })}</Typography>
          <img
            src={resourceProps.content}
            alt={resourceProps.resourceName}
            style={{ maxWidth: '100%', maxHeight: '200px', objectFit: 'contain' }}
          />
        </div>
      )}
    </>
  );
};
