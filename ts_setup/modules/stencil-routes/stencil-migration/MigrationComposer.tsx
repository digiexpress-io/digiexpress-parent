import React from 'react';
import { useSnackbar } from 'notistack';
import { FormattedMessage } from 'react-intl';
import { Button, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material'
import * as Burger from '@dxs-ts/eveli-primitives';
import { CancelButton } from '@dxs-ts/eveli-primitives';
import { StencilComposerApi as Composer } from '@dxs-ts/stencil-api';

const MigrationComposer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { enqueueSnackbar } = useSnackbar();
  const [file, setFile] = React.useState<string | undefined>();
  const [loading, setLoading] = React.useState<boolean | undefined>();
  const { service, actions } = Composer.useComposer();

  const handleCreate = () => {
    if (!file) {
      return;
    }
    setLoading(true);
    service.create().importData(file)
      .then(() => actions.handleLoadSite())
      .then(() => {
        setLoading(false);
        setFile(undefined);
        onClose();
      });
    enqueueSnackbar(message, { variant: 'success' });
  }

  const message = <FormattedMessage id="snack.migration.createdMessage" />

  return (
  <Dialog open={true} onClose={onClose}>
    <DialogTitle><FormattedMessage id='toolbar.import' /></DialogTitle>
    <DialogContent>
      <div>{file}</div>
      <Burger.FileField value='' onChange={setFile} label="imports.select" />
    </DialogContent>
    <DialogActions>
      <CancelButton onClick={onClose} />
      <Button onClick={handleCreate} disabled={loading || !file }>
          <FormattedMessage id='imports.import.action' />
      </Button>
    </DialogActions>
  </Dialog>
  );
}

export { MigrationComposer };
