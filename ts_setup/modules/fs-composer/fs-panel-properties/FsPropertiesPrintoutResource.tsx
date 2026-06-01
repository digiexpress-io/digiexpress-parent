import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs } from '@dxs-ts/fs-api';
import { useUtilityClasses } from './useUtilityClasses';

export interface FsPropertiesPrintoutResourceProps {
  dirent: Fs.DirentBase;
}

export const FsPropertiesPrintoutResource: React.FC<FsPropertiesPrintoutResourceProps> = ({ dirent }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  if (dirent.type !== 'PRINTOUT_RESOURCE') {
    return undefined;
  }

  const resourceProps = dirent.props as Fs.PrintoutResourceProps;

  return (
    <>
      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.resourceContentType' })}</Typography>
        <Typography className={classes.propertyValue}>{resourceProps.contentType}</Typography>
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
