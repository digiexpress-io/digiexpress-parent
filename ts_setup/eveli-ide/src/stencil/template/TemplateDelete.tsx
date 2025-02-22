import React from 'react';

import { FormattedMessage } from 'react-intl';
import { useSnackbar } from 'notistack';

import { Dialog, DialogTitle, DialogContent, DialogActions, Button } from '@mui/material';


import { Composer, StencilApi } from '../context';


interface TemplateDeleteProps {
  templateId: StencilApi.TemplateId;
  onClose: () => void;
}


const TemplateDelete: React.FC<TemplateDeleteProps> = ({ templateId, onClose }) => {
  const { enqueueSnackbar } = useSnackbar();
  const { service, actions } = Composer.useComposer();

  const handleDelete = () => {
    service.delete().template(templateId).then(success => {
      enqueueSnackbar(message, { variant: 'warning' });
      console.log(success)
      onClose();
      actions.handleLoadSite();
    })
    return null;
  }

  const message = <FormattedMessage id="snack.template.deletedMessage" />


  return (
    <Dialog open={true} onClose={onClose}>
    <DialogTitle><FormattedMessage id='template.delete' /></DialogTitle>
    <DialogContent><FormattedMessage id='template.delete.message' /></DialogContent>
    <DialogActions>
      <Button variant='text' onClick={onClose}>
        <FormattedMessage id='button.cancel'/>
      </Button>
      <Button onClick={handleDelete}>
        <FormattedMessage id='button.delete'/>
      </Button>
    </DialogActions>
  </Dialog>
  );
}

export { TemplateDelete }