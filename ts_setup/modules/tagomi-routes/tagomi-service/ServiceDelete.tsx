import React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { useSnackbar } from 'notistack';

import { CancelButton } from '@dxs-ts/eveli-primitives';
import { TagomiApi, TagomiComposerApi as Composer } from '@dxs-ts/tagomi-api';


interface ServiceDeleteProps {
  serviceId: TagomiApi.ServiceId;
  onClose: () => void;
}

export const ServiceDelete: React.FC<ServiceDeleteProps> = ({ serviceId, onClose }) => {
  const { site, actions, backend } = Composer.useComposer();
  const { enqueueSnackbar } = useSnackbar();
  const service = site.services[serviceId];

  const message = <FormattedMessage id="snack.service.deletedMessage" />

  backend.deleteService
  const handleDelete = () => {
    backend.deleteService(service.id).then(_success => {
      enqueueSnackbar(message, { variant: 'warning' });
      onClose();
      actions.handleLoadSite();
    });
  }
  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle><FormattedMessage id='tagomi.service.delete.title' /></DialogTitle>
      <DialogContent>
        <Typography>{service.serviceName}</Typography>
        <Typography><FormattedMessage id='tagomi.service.delete.description' /></Typography>
      </DialogContent>
      <DialogActions>
        <CancelButton onClick={onClose} />
        <Button onClick={handleDelete} >
          <FormattedMessage id='tagomi.button.delete.service' />
        </Button>
      </DialogActions>
    </Dialog>
  );
};
