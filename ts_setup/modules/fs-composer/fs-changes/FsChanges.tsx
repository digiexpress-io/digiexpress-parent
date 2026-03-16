import React from 'react';
import { Typography, IconButton, Tooltip, Dialog, DialogActions, Button, DialogTitle, DialogContent } from '@mui/material';
import { FsIcon, FsIcons } from '../fs-theme';
import { useIntl } from 'react-intl';

import { FsPanel } from '../fs-panel';
import { FsChangesProps, assetsWithChanges } from './FsChangesProps';
import { useOwnerState } from './useOwnerState';
import { FsChangesRoot, useUtilityClasses } from './useUtilityClasses';


export const FsChanges: React.FC<FsChangesProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props)
  const { confirmOpen, isDarkMode, setConfirmOpen, getStatusColor } = ownerState;
  const classes = useUtilityClasses();


  return (
    <FsPanel title={intl.formatMessage({ id: 'fs.changesView.title' })} icon={<FsIcon icon={FsIcons.Save} large />} activeNode={true}>
      <div className={classes.actionBar}>
        <Button variant='outlined' className={classes.discardButton}>{intl.formatMessage({ id: 'fs.changesView.discardAll' })}</Button>
        <Button>{intl.formatMessage({ id: 'fs.changesView.saveAll' })}</Button>
      </div>
      {confirmOpen && <UndoConfirmDialog open={confirmOpen} onClose={() => setConfirmOpen(false)} />}
      <FsChangesRoot className={classes.root} ownerState={ownerState}>
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
      </FsChangesRoot>
    </FsPanel>
  );
};


const UndoConfirmDialog: React.FC<{ open: boolean, onClose: () => void }> = ({ onClose }) => {
  const intl = useIntl();

  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle>{intl.formatMessage({ id: 'fs.changesView.undoConfirmDialog.title' })}</DialogTitle>
      <DialogContent>{intl.formatMessage({ id: 'fs.changesView.undoConfirmDialog.content' })}</DialogContent>
      <DialogActions>
        <Button variant='outlined' onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button onClick={onClose}>{intl.formatMessage({ id: 'button.accept' })}</Button>
      </DialogActions>
    </Dialog>
  )
}


