import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { useFsDirent } from '@dxs-ts/fs-api';
import { usePanelProperties } from '../fs-panel-properties';

export interface FsPropertiesPrintoutResourceProps {
  direntId: string;
}

export const FsPropertiesPrintoutResource: React.FC<FsPropertiesPrintoutResourceProps> = ({ direntId }) => {
  const intl = useIntl();
  const classes = usePanelProperties();
  const { getDirent } = useFsDirent();

  const dirent = getDirent(direntId);

  if (!dirent || dirent.type !== 'PRINTOUT_RESOURCE') {
    return undefined;
  }


  return (
    <>
      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.resourceContentType' })}</Typography>
      </div>

    </>
  );
};
