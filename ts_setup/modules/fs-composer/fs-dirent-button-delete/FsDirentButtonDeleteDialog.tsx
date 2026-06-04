import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Typography } from '@mui/material';
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
  const type = asset ? intl.formatMessage({ id: `fs.bodyType.${asset.type}` }) : '';

  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>{intl.formatMessage({ id: 'fs.direntMenu.deleteDialog.title' }, { type })}</DialogTitle>
      <DialogContent>
        <Typography>{intl.formatMessage({ id: 'fs.direntMenu.deleteDialog.message' }, { name: asset?.name ?? assetId })}</Typography>
      </DialogContent>
      <DialogActions>
        <Button variant='outlined' onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button onClick={onConfirm} color='error'>{intl.formatMessage({ id: 'button.confirmDelete' })}</Button>
      </DialogActions>
    </Dialog>
  );
};
