import React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { useSnackbar } from 'notistack';

import { CancelButton } from '@dxs-ts/eveli-primitives';
import { TagomiComposerApi as Composer } from '@dxs-ts/tagomi-api';


interface LogoDeleteProps {
  logoId: string;
  onClose: () => void;
}

export const LogoDelete: React.FC<LogoDeleteProps> = ({ logoId, onClose }) => {
  const { site, actions, backend } = Composer.useComposer();
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();
  const logo = site.resources[logoId];

  const message = intl.formatMessage({ id: 'snack.logo.deletedMessage' });

  const handleDelete = () => {
    backend.deleteResource(logo.id).then(_success => {
      enqueueSnackbar(message, { variant: 'success' });
      onClose();
      actions.handleLoadSite();
    });
  }

  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle>{intl.formatMessage({ id: 'tagomi.logo.delete.dialog.title' })}{" "}{logo.resourceName}</DialogTitle>
      <DialogContent>
        <Typography>{intl.formatMessage({ id: 'tagomi.logo.delete.dialog.desc' })}</Typography>
      </DialogContent>
      <DialogActions>
        <CancelButton onClick={onClose} />
        <Button onClick={handleDelete}>
          {intl.formatMessage({ id: 'tagomi.logo.delete.dialog.button' })}
        </Button>
      </DialogActions>
    </Dialog>
  );
};
