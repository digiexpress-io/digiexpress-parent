import React from 'react';
import { useSnackbar } from 'notistack';

import { Dialog, DialogTitle, DialogContent, DialogActions, Button } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { StencilComposerApi as Composer } from '@dxs-ts/stencil-api';
import { StencilApi } from '@dxs-ts/stencil-api';
import { CancelButton } from '@dxs-ts/eveli-primitives';


interface LinkDeleteProps {
  linkId: StencilApi.LinkId,
  onClose: () => void,
}

const LinkDelete: React.FC<LinkDeleteProps> = ({ linkId, onClose }) => {
  const { enqueueSnackbar } = useSnackbar();
  const { service, actions } = Composer.useComposer();

  const message = <FormattedMessage id="snack.link.deletedMessage" />
  const handleDelete = () => {
    service.delete().link(linkId).then(success => {
      enqueueSnackbar(message, { variant: 'warning' });
      console.log(success)
      onClose();
      actions.handleLoadSite();
    })
  }




  return (
    <Dialog open={true} onClose={onClose}>
    <DialogTitle><FormattedMessage id='link.delete.title' /></DialogTitle>
    <DialogContent><FormattedMessage id="link.delete" /></DialogContent>
    <DialogActions>
      <CancelButton onClick={onClose} />
      <Button onClick={handleDelete}>
          <FormattedMessage id='button.delete.link' />
      </Button>
    </DialogActions>
  </Dialog>
  );
}
export { LinkDelete }
