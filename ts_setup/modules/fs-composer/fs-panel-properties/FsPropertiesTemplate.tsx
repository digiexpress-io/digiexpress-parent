import React from 'react';
import { Typography, Box } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs } from '@dxs-ts/fs-api';
import { useUtilityClasses } from './useUtilityClasses';


export interface FsPropertiesTemplateProps {
  dirent: Fs.DirentBase;
}

export const FsPropertiesTemplate: React.FC<FsPropertiesTemplateProps> = ({ dirent }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

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
