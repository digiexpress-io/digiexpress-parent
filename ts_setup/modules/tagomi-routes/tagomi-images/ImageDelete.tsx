import React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { useSnackbar } from 'notistack';

import { CancelButton } from '@dxs-ts/eveli-primitives';
import { TagomiComposerApi as Composer } from '@dxs-ts/tagomi-api';


interface ImageDeleteProps {
  imageId: string;
  onClose: () => void;
}

export const ImageDelete: React.FC<ImageDeleteProps> = ({ imageId, onClose }) => {
  const { site, actions, backend } = Composer.useComposer();
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();
  const image = site.resources[imageId];

  const message = intl.formatMessage({ id: 'snack.image.deletedMessage' });

  const handleDelete = () => {
    backend.deleteResource(image.id).then(_success => {
      enqueueSnackbar(message, { variant: 'success' });
      onClose();
      actions.handleLoadSite();
    });
  }

  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle>{intl.formatMessage({ id: 'tagomi.image.delete.dialog.title' })}{" "}{image.resourceName}</DialogTitle>
      <DialogContent>
        <Typography>{intl.formatMessage({ id: 'tagomi.image.delete.dialog.desc' })}</Typography>
      </DialogContent>
      <DialogActions>
        <CancelButton onClick={onClose} />
        <Button onClick={handleDelete}>
          {intl.formatMessage({ id: 'tagomi.image.delete.dialog.button' })}
        </Button>
      </DialogActions>
    </Dialog>
  );
};
