import React from 'react';
import { Typography, Box } from '@mui/material';
import { useIntl } from 'react-intl';
import { useFsDirent } from '@dxs-ts/fs-api';
import { usePanelProperties } from '../fs-panel-properties';


export interface FsPropertiesTemplateProps {
  direntId: string;
}

export const FsPropertiesTemplate: React.FC<FsPropertiesTemplateProps> = ({ direntId }) => {
  const intl = useIntl();
  const classes = usePanelProperties();
  const { getDirent } = useFsDirent();

  const dirent = getDirent(direntId);

  if (!dirent) {
    return;
  }

  if (dirent.type !== 'ARTICLE_TEMPLATE') {
    return undefined;
  }

  const configOptionsEnabled = dirent.props?.configOptions ?? [];

  return (
    <div className={classes.propertyRow}>
      <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.configOptionsEnabled' })}</Typography>
      <div className={classes.propertyList}>
        {configOptionsEnabled.map((option, index) => (
          <Box key={index} className={classes.configOptionsListItem}>
            {intl.formatMessage({ id: `fs.dirent.configOption.${option}` })}
          </Box>
        ))}
      </div>
    </div>
  );
};
