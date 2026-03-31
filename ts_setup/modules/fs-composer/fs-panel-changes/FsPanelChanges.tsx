import React from 'react';
import { Typography, IconButton, Tooltip, Button } from '@mui/material';
import { FsIcon, FsIcons } from '../fs-theme';
import { useIntl } from 'react-intl';

import { FsPanel } from '../fs-panel';
import { FsPanelChangesProps } from './FsPanelChangesProps';
import { useOwnerState } from './useOwnerState';
import { FsPanelChangesRoot, useUtilityClasses } from './useUtilityClasses';
import { UndoConfirmDialog } from './FsChangesConfirmDialog';


export const FsPanelChanges: React.FC<FsPanelChangesProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props)
  const { confirmOpen, isDarkMode, setConfirmOpen, getStatusColor } = ownerState;
  const classes = useUtilityClasses();


  return (
    <FsPanel title={intl.formatMessage({ id: 'fs.changesView.title' })} icon={<FsIcon icon={FsIcons.Save} large />} activeDirent={true}>
      <div className={classes.actionBar}>
        <Button variant='outlined' className={classes.discardButton}>{intl.formatMessage({ id: 'fs.changesView.discardAll' })}</Button>
        <Button>{intl.formatMessage({ id: 'fs.changesView.saveAll' })}</Button>
      </div>
      {confirmOpen && <UndoConfirmDialog open={confirmOpen} onClose={() => setConfirmOpen(false)} />}
      <FsPanelChangesRoot className={classes.root} ownerState={ownerState}>
        {assetsWithChanges.map((asset) => (
          <div key={asset.id} className={classes.changeRow}>
            <Typography className={classes.assetName}>
              {asset.name}
            </Typography>
            <Typography className={classes.statusText} style={{ color: getStatusColor(asset.status, isDarkMode) }}>
              {asset.status}
            </Typography>

            <Tooltip title={intl.formatMessage({ id: 'fs.changesView.undo' })}>
              <IconButton size="small" onClick={() => setConfirmOpen(true)} className={classes.undoButton}>
                <FsIcon icon={FsIcons.Undo} medium />
              </IconButton>
            </Tooltip>
          </div>
        ))}
      </FsPanelChangesRoot>
    </FsPanel>
  );
};




const assetsWithChanges = [
  { id: 'main.article', name: 'main.article', status: 'modified' },
  { id: 'info-gdpr.article', name: 'info-gdpr.article', status: 'modified' },
  { id: 'general-message.service', name: 'general-message.service', status: 'modified' },
  { id: 'taskMsgFlow.flow', name: 'taskMsgFlow.flow', status: 'new' },
  { id: 'public-inforeq.service', name: 'public-inforeq.service', status: 'deleted' },
  { id: 'trustee-info-form.service', name: 'trustee-info-form.service', status: 'modified' },
  { id: 'sipoo-main-site.link', name: 'sipoo-main-site.link', status: 'new' }
];
