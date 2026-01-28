import React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { useSnackbar } from 'notistack';

import { CancelButton } from '@dxs-ts/eveli-primitives';
import { TagomiComposerApi as Composer } from '@dxs-ts/tagomi-api';


interface ScriptDeleteProps {
  scriptId: string;
  onClose: () => void;
}

export const ScriptDelete: React.FC<ScriptDeleteProps> = ({ scriptId, onClose }) => {
  const { site, actions, backend } = Composer.useComposer();
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();
  const script = site.resources[scriptId];

  const message = intl.formatMessage({ id: 'snack.script.deletedMessage' });

  const handleDelete = () => {
    backend.deleteResource(script.id).then(_success => {
      enqueueSnackbar(message, { variant: 'success' });
      onClose();
      actions.handleLoadSite();
    });
  }

  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle>{intl.formatMessage({ id: 'tagomi.script.delete.dialog.title' })}{" "}{script.resourceName}</DialogTitle>
      <DialogContent>
        <Typography>{intl.formatMessage({ id: 'tagomi.script.delete.dialog.desc' })}</Typography>
      </DialogContent>
      <DialogActions>
        <CancelButton onClick={onClose} />
        <Button onClick={handleDelete}>
          {intl.formatMessage({ id: 'tagomi.script.delete.dialog.button' })}
        </Button>
      </DialogActions>
    </Dialog>
  );
};
