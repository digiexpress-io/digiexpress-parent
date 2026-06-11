import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs } from '@dxs-ts/fs-api';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsPanel } from '../fs-panel';
import { useOwnerState } from './useOwnerState';
import { FsPanelArticleOrderRoot, useUtilityClasses } from './useUtilityClasses';


export const FsPanelArticleOrder: React.FC = () => {
  const intl = useIntl();
  const ownerState = useOwnerState();
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
                {String((entry.props as Fs.ArticleProps)?.orderNumber ?? 0).padStart(3, '0')}
              </Typography>
              <Typography className={classes.name}>{ownerState.getDirentName(entry.id)}</Typography>
              {entry.props?.assetDescription && (
                <Typography className={classes.description}>{entry.props?.assetDescription}</Typography>
              )}
            </div>
          ))}
        </div>
      </FsPanelArticleOrderRoot>
    </FsPanel>
  );
};
