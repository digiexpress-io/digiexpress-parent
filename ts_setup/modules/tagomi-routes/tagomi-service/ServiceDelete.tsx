import React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { useSnackbar } from 'notistack';

import { CancelButton } from '@dxs-ts/eveli-primitives';
import { TagomiApi, TagomiComposerApi as Composer } from '@dxs-ts/tagomi-api';


interface ServiceDeleteProps {
  serviceId: TagomiApi.ServiceId;
  onClose: () => void;
}

export const ServiceDelete: React.FC<ServiceDeleteProps> = ({ serviceId, onClose }) => {
  const { site, actions, backend } = Composer.useComposer();
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();
  const service = site.services[serviceId];

  const message = intl.formatMessage({ id: 'snack.service.deletedMessage' });

  backend.deleteService
  const handleDelete = () => {
    backend.deleteService(service.id).then(_success => {
      enqueueSnackbar(message, { variant: 'success' });
      onClose();
      actions.handleLoadSite();
    });
  }
  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle>{intl.formatMessage({ id: 'tagomi.service.delete.dialog.title' })}{" "}{service.serviceName}</DialogTitle>
      <DialogContent>
        <Typography>{intl.formatMessage({ id: 'tagomi.service.delete.dialog.desc' })}</Typography>
      </DialogContent>
      <DialogActions>
        <CancelButton onClick={onClose} />
        <Button onClick={handleDelete} >
          {intl.formatMessage({ id: 'tagomi.service.delete.dialog.button' })}
        </Button>
      </DialogActions>
    </Dialog>
  );
};
