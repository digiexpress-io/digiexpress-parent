import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { useFsDirent } from '@dxs-ts/fs-api';
import { usePanelProperties } from '../fs-panel-properties';

export interface FsPropertiesFlowProps {
  direntId: string;
}

export const FsPropertiesFlow: React.FC<FsPropertiesFlowProps> = ({ direntId }) => {
  const intl = useIntl();
  const classes = usePanelProperties();
  const { getDirent } = useFsDirent();

  const dirent = getDirent(direntId);

  if (!dirent || dirent.type !== 'FLOW') {
    return undefined;
  }

  const description = dirent.props?.assetDescription;
  const labels = dirent.props?.labels ?? [];

  return (
    <>
      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.description' })}</Typography>
        <Typography className={classes.propertyValue}>{description}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.labels' })}</Typography>
        <div className={classes.propertyList}>
          {labels.map((label, index) => (
            <div key={index} className={classes.label}>
              <Typography component="span">{label.key}</Typography>
            </div>
          ))}
        </div>
      </div>
    </>
  );
};
