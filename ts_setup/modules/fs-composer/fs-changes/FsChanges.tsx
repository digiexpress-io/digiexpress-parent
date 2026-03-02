import React from 'react';
import { Typography, IconButton, Tooltip, Dialog, DialogActions, Button, DialogTitle, DialogContent } from '@mui/material';
import { FsIcons } from '../fs-theme';
import { ViewContainer } from '../fs-main-views';
import { FsChangesProps, assetsWithChanges } from './FsChangesProps';
import { useOwnerState } from './useOwnerState';
import { FsChangesRoot, useUtilityClasses } from './useUtilityClasses';


export const FsChanges: React.FC<FsChangesProps> = (props) => {
  const ownerState = useOwnerState(props)
  const { confirmOpen, isDarkMode, setConfirmOpen, getStatusColor } = ownerState;
  const classes = useUtilityClasses();


  return (
    <ViewContainer title='Unsaved changes' icon={<FsIcons.Save />} activeNode={true}>
      <div className={classes.actionBar}>
        <Button variant='outlined' className={classes.discardButton}>Discard all changes</Button>
        <Button>Save all changes</Button>
      </div>
      {confirmOpen && <UndoConfirmDialog open={confirmOpen} onClose={() => setConfirmOpen(false)} />}
      <FsChangesRoot className={classes.root} ownerState={ownerState}>
        {assetsWithChanges.map((asset) => (
          <div key={asset.id} className={classes.changeRow}>
            <Typography variant="subtitle2" className={classes.assetName}>
              {asset.name}
            </Typography>
            <Typography variant="subtitle2" className={classes.statusText} style={{ color: getStatusColor(asset.status, isDarkMode) }}>
              {asset.status}
            </Typography>

            <Tooltip title="Undo changes">
              <IconButton
                size="small"
                onClick={() => setConfirmOpen(true)}
                className={classes.undoButton}
              >
                <FsIcons.Undo />
              </IconButton>
            </Tooltip>
          </div>
        ))}
      </FsChangesRoot>
    </ViewContainer>
  );
};


const UndoConfirmDialog: React.FC<{ open: boolean, onClose: () => void }> = ({ onClose }) => {
  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle>Undo changes to asset</DialogTitle>
      <DialogContent>
        You are about to discard all changes to this file. This action cannot be undone.
      </DialogContent>
      <DialogActions>
        <Button variant='outlined' onClick={onClose}>Cancel</Button>
        <Button onClick={onClose}>Accept</Button>
      </DialogActions>
    </Dialog>
  )
}


