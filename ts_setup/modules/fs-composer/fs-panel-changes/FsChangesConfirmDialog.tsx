import { Button, Dialog, DialogActions, DialogContent, DialogTitle } from '@mui/material';
import React from 'react';
import { useIntl} from 'react-intl';


export const UndoConfirmDialog: React.FC<{ open: boolean, onClose: () => void, onConfirm: () => void }> = ({ onClose, onConfirm }) => {
  const intl = useIntl();

  function handleConfirm() {
    onConfirm();
    onClose();
  }

  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle>{intl.formatMessage({ id: 'fs.changesView.undoConfirmDialog.title' })}</DialogTitle>
      <DialogContent>{intl.formatMessage({ id: 'fs.changesView.undoConfirmDialog.content' })}</DialogContent>
      <DialogActions>
        <Button variant='outlined' onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button onClick={handleConfirm}>{intl.formatMessage({ id: 'button.accept' })}</Button>
      </DialogActions>
    </Dialog>
  )
}