import React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { useSnackbar } from 'notistack';

import { Composer, StencilApi } from '../context';


interface ReleaseDeleteProps {
  id: StencilApi.ReleaseId;
  onClose: () => void;
}

const ReleaseDelete: React.FC<ReleaseDeleteProps> = ({ id, onClose }) => {
  const { service, actions } = Composer.useComposer();
  const { enqueueSnackbar } = useSnackbar();
  const message = <FormattedMessage id="snack.release.deletedMessage" />

  const handleDelete = () => {
    service.delete().release(id).then(_success => {
      enqueueSnackbar(message, {variant: 'warning'});
      onClose();
      actions.handleLoadSite();
    });
  }

  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle><FormattedMessage id='release.delete.title' /></DialogTitle>
      <DialogContent><FormattedMessage id="release.delete.desc" /></DialogContent>
      <DialogActions>
        <Button variant='text' onClick={onClose}>
          <FormattedMessage id='button.cancel'/>
        </Button>
        <Button onClick={handleDelete}>
          <FormattedMessage id='button.delete'/>
        </Button>
      </DialogActions>
    </Dialog>);
};
export { ReleaseDelete };
