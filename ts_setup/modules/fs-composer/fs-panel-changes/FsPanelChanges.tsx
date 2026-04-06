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
  const { confirmOpen, isDarkMode, setConfirmOpen, getStatusColor, changes } = ownerState;
  const classes = useUtilityClasses();


  return (
    <FsPanel title={intl.formatMessage({ id: 'fs.changesView.title' })} icon={<FsIcon icon={FsIcons.Save} large />} activeDirent={true}>
      <div className={classes.actionBar}>
        <Button variant='outlined' className={classes.discardButton}>{intl.formatMessage({ id: 'fs.changesView.discardAll' })}</Button>
        <Button>{intl.formatMessage({ id: 'fs.changesView.saveAll' })}</Button>
      </div>
      {confirmOpen && <UndoConfirmDialog open={confirmOpen} onClose={() => setConfirmOpen(false)} />}
      <FsPanelChangesRoot className={classes.root} ownerState={ownerState}>
        {changes.map((asset) => (
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




