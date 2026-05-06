import React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { useSnackbar } from 'notistack';

import { CancelButton } from '@dxs-ts/eveli-primitives';
import { TagomiApi, TagomiComposerApi as Composer } from '@dxs-ts/tagomi-api';
import { useTagomiNav, useTagomiTabClose } from '../tagomi-nav';


interface ServiceDeleteProps {
  serviceId: TagomiApi.ServiceId;
  onClose: () => void;
}

export const ServiceDelete: React.FC<ServiceDeleteProps> = ({ serviceId, onClose }) => {
  const { site, actions, backend } = Composer.useComposer();
  const { explorer } = useTagomiNav();
  const { onTabClose } = useTagomiTabClose();
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();
  const service = site.services[serviceId];

  const message = intl.formatMessage({ id: 'snack.service.deletedMessage' });

  const handleDelete = async () => {
    await backend.deleteService(service.id);

    explorer
      .filter(tab => (tab.type === 'SERVICE_TEMPLATES' || tab.type === 'SERVICES') && tab.service === service.id)
      .forEach(tab => onTabClose(tab));

    enqueueSnackbar(message, { variant: 'success' });
    onClose();
    actions.handleLoadSite();
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
