import React from 'react';
import { Typography } from '@mui/material';
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

  return undefined;
};
