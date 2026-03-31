import { Button, Dialog, DialogActions, DialogContent, DialogTitle } from '@mui/material';
import React from 'react';
import { useIntl} from 'react-intl';


export const UndoConfirmDialog: React.FC<{ open: boolean, onClose: () => void }> = ({ onClose }) => {
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