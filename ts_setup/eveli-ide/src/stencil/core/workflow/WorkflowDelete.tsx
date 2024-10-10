import React from 'react';
import { DialogContentText } from '@mui/material';
import { useSnackbar } from 'notistack';

import { FormattedMessage } from 'react-intl';

import { Composer, StencilClient } from '../context';
import Burger from '@the-wrench-io/react-burger';

interface WorkflowDeleteProps {
  workflow: StencilClient.Workflow,
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
    <Burger.Dialog open={true} onClose={onClose}
      backgroundColor="uiElements.main"
      title="services.delete"
      submit={{ title: "button.delete", onClick: handleDelete, disabled: false }}>

      <DialogContentText>
        <FormattedMessage id="services.delete.desc" />
      </DialogContentText>
    </Burger.Dialog>
  );
}
export { WorkflowDelete }
