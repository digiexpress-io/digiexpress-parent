import React from 'react';
import { Button, Dialog, DialogTitle, DialogContent, DialogActions, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs } from '@dxs-ts/fs-api';

export interface FsDirentMenuDeleteDialogProps {
  dirent: Fs.DirentBase;
  onClose: () => void;
  onConfirm: () => void;
}

export const FsDirentMenuDeleteDialog: React.FC<FsDirentMenuDeleteDialogProps> = (props) => {
  const intl = useIntl();
  const type = intl.formatMessage({ id: `fs.bodyType.${props.dirent.type}` });

  return (
    <Dialog open onClose={props.onClose}>
      <DialogTitle>{intl.formatMessage({ id: 'fs.direntMenu.deleteDialog.title' }, { type })}</DialogTitle>
      <DialogContent>
        <Typography>{intl.formatMessage({ id: 'fs.direntMenu.deleteDialog.message' }, { name: props.dirent.name })}</Typography>
      </DialogContent>
      <DialogActions>
        <Button variant='outlined' onClick={props.onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button onClick={props.onConfirm} color='error'>{intl.formatMessage({ id: 'button.confirmDelete' })}</Button>
      </DialogActions>
    </Dialog>
  );
};
