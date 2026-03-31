import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsPanel } from '../fs-panel';
import { FsPanelArticleOrderProps } from './FsPanelArticleOrderProps';
import { useOwnerState } from './useOwnerState';
import { FsPanelArticleOrderRoot, useUtilityClasses } from './useUtilityClasses';


export const FsPanelArticleOrder: React.FC<FsPanelArticleOrderProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsPanel
      title={intl.formatMessage({ id: 'fs.articleOrder.title' })}
      icon={<FsIcon icon={FsIcons.ArticleOrder} large />}
      activeDirent={true}
    >
      <FsPanelArticleOrderRoot className={classes.root} ownerState={ownerState}>
        <div className={classes.header}>
          <Typography className={classes.orderNumber}>{intl.formatMessage({ id: 'fs.articleOrder.col.position' })}</Typography>
          <Typography className={classes.name}>{intl.formatMessage({ id: 'fs.articleOrder.col.name' })}</Typography>
          <Typography className={classes.description}>{intl.formatMessage({ id: 'fs.articleOrder.col.description' })}</Typography>
        </div>
        <div className={classes.container}>
          {ownerState.articles.map((entry) => (
            <div key={entry.id} className={classes.row}>
              <Typography className={classes.orderNumber}>
                {String(entry.orderNumber).padStart(3, '0')}
              </Typography>
              <Typography className={classes.name}>{entry.name}</Typography>
              {entry.description && (
                <Typography className={classes.description}>{entry.description}</Typography>
              )}
            </div>
          ))}
        </div>
      </FsPanelArticleOrderRoot>
    </FsPanel>
  );
};
