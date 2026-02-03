import React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Box, Alert } from '@mui/material';
import { useSnackbar } from 'notistack';
import { useIntl } from 'react-intl';
import MonacoReact, { OnChange } from '@monaco-editor/react';

import * as Burger from '@dxs-ts/eveli-primitives';
import { TagomiComposerApi as Composer, TagomiApi } from '@dxs-ts/tagomi-api';
import { CancelButton } from '@dxs-ts/eveli-primitives';


export const ScriptComposer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { backend, actions } = Composer.useComposer();
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();
  const [resourceName, setResourceName] = React.useState("");
  const [uploadBody, setUploadBody] = React.useState("");
  const [error, setError] = React.useState<string>();

  const message = intl.formatMessage({ id: 'snack.script.createdMessage' }, { resourceName });

  const handleChange: OnChange = (newValue) => {
    const scriptContent = newValue ?? '';
    setUploadBody(scriptContent);
  };

  const handleCreate = () => {
    setError(undefined);
    const entity: TagomiApi.CreateResource = {
      resourceName,
      contentType: TagomiApi.ResourceType.SCRIPT,
      uploadBody,
      templateIds: []
    };

    backend.createResource(entity)
      .then(_success => {
        enqueueSnackbar(message, { variant: 'success' });
        onClose();
        actions.handleLoadSite();
      })
      .catch((err: any) => {
        const errorMessage = err?.message || err?.toString() || 'Failed to create script';
        setError(errorMessage);
        enqueueSnackbar(errorMessage, { variant: 'error' });
      });
  };

  return (
    <Dialog open={true} onClose={onClose} maxWidth="lg" fullWidth>
      <DialogTitle>{intl.formatMessage({ id: 'tagomi.script.create.dialog.title' })}</DialogTitle>

      <DialogContent>
        {!!error && (
          <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>
        )}

        <Burger.TextField
          label='tagomi.script.create.dialog.resourceName'
          onChange={setResourceName}
          value={resourceName ? resourceName : ''}
        />

        <Box mb={2} />

        <Box height="400px">
          <MonacoReact
            onChange={handleChange}
            value={uploadBody}
            defaultLanguage='yaml'
            options={{
              wordBasedSuggestions: 'off',
              minimap: { enabled: false }
            }}
          />
        </Box>
      </DialogContent>

      <DialogActions>
        <CancelButton onClick={onClose} />
        <Button onClick={handleCreate} disabled={!resourceName || !uploadBody}>
          {intl.formatMessage({ id: 'button.save' })}
        </Button>
      </DialogActions>
    </Dialog>
  );
};
