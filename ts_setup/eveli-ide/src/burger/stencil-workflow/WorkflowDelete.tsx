import React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button } from '@mui/material';
import { useSnackbar } from 'notistack';
import { FormattedMessage } from 'react-intl';
import { StencilComposerApi as Composer } from '../stencil-setup';
import { StencilApi } from '@/burger';


interface WorkflowDeleteProps {
  workflow: StencilApi.Workflow,
  onClose: () => void,
}

const WorkflowDelete: React.FC<WorkflowDeleteProps> = ({ workflow, onClose }) => {
  const { enqueueSnackbar } = useSnackbar();
  const { service, actions } = Composer.useComposer();

  const handleDelete = () => {
    service.delete().workflow(workflow.id).then(success => {
      enqueueSnackbar(message, { variant: 'warning' });
      console.log(success)
      onClose();
      actions.handleLoadSite();
    })
  }
  const message = <FormattedMessage id="snack.workflow.deletedMessage" />

  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle><FormattedMessage id='services.delete' /></DialogTitle>
      <DialogContent><FormattedMessage id="services.delete.desc" /></DialogContent>
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
export { WorkflowDelete }
