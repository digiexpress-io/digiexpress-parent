import { Button, Dialog, DialogActions, DialogContent, DialogTitle } from '@mui/material';
import React from 'react';
import { useIntl } from 'react-intl';
import { useFsDirent } from "@dxs-ts/fs-api";


export interface FsDirentButtonDeleteDialogProps {
  open: boolean;
  assetId: string;
  onClose: () => void;
  onConfirm: () => void;
}

export const FsDirentButtonDeleteDialog: React.FC<FsDirentButtonDeleteDialogProps> = ({ assetId, open, onClose, onConfirm }) => {
  const intl = useIntl();
  const { getDirent } = useFsDirent();

  const asset = getDirent(assetId);

  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>{intl.formatMessage({ id: 'fs.dirent.deleteConfirmDialog.title' })}{asset ? `: ${asset.name}` : ''}</DialogTitle>
      <DialogContent>{intl.formatMessage({ id: 'fs.dirent.deleteConfirmDialog.content' })}</DialogContent>
      <DialogActions>
        <Button variant='outlined' onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button onClick={onConfirm}>{intl.formatMessage({ id: 'button.delete' })}</Button>
      </DialogActions>
    </Dialog>
  );
};
