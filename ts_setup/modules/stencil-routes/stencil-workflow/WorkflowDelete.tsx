import React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Typography, Box } from '@mui/material';
import { useSnackbar } from 'notistack';
import { FormattedMessage, useIntl } from 'react-intl';
import { StencilComposerApi as Composer } from '@dxs-ts/stencil-api';
import { StencilApi } from '@dxs-ts/stencil-api';
import { CancelButton } from '@dxs-ts/eveli-primitives';
import { WorkflowDeleteRoot, useWorkflowDeleteUtilityClasses } from './useUtilityClasses';

interface WorkflowDeleteProps {
  workflow: StencilApi.Workflow;
  onClose: () => void;
}

const WorkflowDelete: React.FC<WorkflowDeleteProps> = ({ workflow, onClose }) => {
  const intl = useIntl();
  const classes = useWorkflowDeleteUtilityClasses();
  const { enqueueSnackbar } = useSnackbar();
  const { service, actions } = Composer.useComposer();

  const workflowName =
    workflow?.body?.value
    ?? (workflow?.body?.flowName || undefined)
    ?? intl.formatMessage({ id: 'service.unknown' });

  const message = <FormattedMessage id="snack.workflow.deletedMessage" />;

  const handleDelete = () => {
    service.delete().workflow(workflow.id).then(success => {
      enqueueSnackbar(message, { variant: 'warning' });
      console.log(success);
      onClose();
      actions.handleLoadSite();
    });
  };

  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle>
        <FormattedMessage id="services.delete" />
      </DialogTitle>

      <DialogContent>
        <Typography className={classes.description}>
          <FormattedMessage id="services.delete.desc" />
        </Typography>

        <WorkflowDeleteRoot className={classes.root}>
          <Box className={classes.infoBox}>
            <Typography variant="body2" component="div">
              <strong className={classes.label}>
                {intl.formatMessage({ id: 'service.name' })}:
              </strong>{' '}
              <span className={classes.value}>{workflowName}</span>
            </Typography>
          </Box>
        </WorkflowDeleteRoot>
      </DialogContent>

      <DialogActions>
        <CancelButton onClick={onClose} />
        <Button onClick={handleDelete} color="error">
          <FormattedMessage id="service.button.delete" />
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export { WorkflowDelete };
