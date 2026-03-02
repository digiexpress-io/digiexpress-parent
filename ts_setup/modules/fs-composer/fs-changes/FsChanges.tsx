import React from 'react';
import { Typography, IconButton, Tooltip, Dialog, DialogActions, Button, DialogTitle, DialogContent } from '@mui/material';
import { FsColors, FsIcons } from '../fs-theme';
import { ViewContainer } from '../fs-main-views';
import { FsChangesProps } from './FsChangesProps';
import { useOwnerState } from './useOwnerState';
import { FsChangesRoot, useUtilityClasses } from './useUtilityClasses';


const assetsWithChanges = [
  { id: 'main.article', name: 'main.article', status: 'modified' },
  { id: 'info-gdpr.article', name: 'info-gdpr.article', status: 'modified' },
  { id: 'general-message.service', name: 'general-message.service', status: 'modified' },
  { id: 'taskMsgFlow.flow', name: 'taskMsgFlow.flow', status: 'new' },
  { id: 'public-inforeq.service', name: 'public-inforeq.service', status: 'deleted' },
  { id: 'trustee-info-form.service', name: 'trustee-info-form.service', status: 'modified' },
  { id: 'sipoo-main-site.link', name: 'sipoo-main-site.link', status: 'new' }
];

const getStatusColor = (status: string, isDarkMode: boolean) => {
  switch (status) {
    case 'deleted':
      return isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight;
    case 'new':
      return FsColors.semantic.success;
    case 'modified':
      return isDarkMode ? FsColors.semantic.warning : FsColors.semantic.warningLight;
    default:
      return isDarkMode ? FsColors.dark.text : FsColors.light.text;
  }
};



export const FsChanges: React.FC<FsChangesProps> = (props) => {
  const { confirmOpen, isDarkMode, setConfirmOpen } = useOwnerState(props)
  const classes = useUtilityClasses();


  return (
    <ViewContainer title='Unsaved changes' icon={<FsIcons.Save />} activeNode={true}>
      <div className={classes.actionBar}>
        <Button variant='outlined' className={classes.discardButton}>Discard all changes</Button>
        <Button>Save all changes</Button>
      </div>
      {confirmOpen && <UndoConfirmDialog open={confirmOpen} onClose={() => setConfirmOpen(false)} />}
      <FsChangesRoot className={classes.root} isDarkMode={isDarkMode}>
        {assetsWithChanges.map((asset) => (
          <div key={asset.id} className={classes.changeRow}>
            <Typography variant="subtitle2" className={classes.assetName}>
              {asset.name}
            </Typography>
            <Typography
              variant="subtitle2"
              className={classes.statusText}
              style={{ color: getStatusColor(asset.status, isDarkMode) }}
            >
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


