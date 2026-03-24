import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { ArticleDirentProps, FsDirent } from '@dxs-ts/fs-api';
import { useUtilityClasses } from './useUtilityClasses';


export interface FsPropertiesArticleProps {
  direntProps: ArticleDirentProps;
  children: FsDirent[];
}

export const FsPropertiesArticle: React.FC<FsPropertiesArticleProps> = ({ children }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  return (
    <div className={classes.propertyRow}>
      <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.children' })}</Typography>
      <div className={classes.commentList}>
        {children.map((child, index) => (
          <Typography key={index} className={classes.propertyValue}>{child.name}</Typography>
        ))}
      </div>
    </div>
  );
};
