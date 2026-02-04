import React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Box, Alert } from '@mui/material';
import { useSnackbar } from 'notistack';
import { useIntl } from 'react-intl';
import MonacoReact, { OnChange } from '@monaco-editor/react';

import * as Burger from '@dxs-ts/eveli-primitives';
import { TagomiComposerApi as Composer, TagomiApi } from '@dxs-ts/tagomi-api';
import { CancelButton } from '@dxs-ts/eveli-primitives';


interface ScriptEditProps {
  scriptId: string;
  onClose: () => void;
}

export const ScriptEdit: React.FC<ScriptEditProps> = ({ scriptId, onClose }) => {
  const { backend, actions, site } = Composer.useComposer();
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();

  const script = site.resources[scriptId];

  const [resourceName, setResourceName] = React.useState(script.resourceName);
  const [uploadBody, setUploadBody] = React.useState(script.content ?? '');
  const [error, setError] = React.useState<string>();

  const message = intl.formatMessage({ id: 'snack.script.editedMessage' }, { resourceName });

  const handleChange: OnChange = (newValue) => {
    const scriptContent = newValue ?? '';
    setUploadBody(scriptContent);
  };

  const handleUpdate = () => {
    setError(undefined);
    const entity: TagomiApi.ResourceMutator = {
      resourceId: scriptId,
      resourceName,
      uploadBody
    };

    backend.updateResource(entity)
      .then(_success => {
        enqueueSnackbar(message, { variant: 'success' });
        onClose();
        actions.handleLoadSite();
      })
  };

  const updateDisabled = !resourceName;

  return (
    <Dialog open={true} onClose={onClose} maxWidth="lg" fullWidth>
      <DialogTitle>{intl.formatMessage({ id: 'tagomi.script.edit.dialog.title' })}{" "}{script.resourceName}</DialogTitle>

      <DialogContent>
        {!!error && (
          <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>
        )}

        <Burger.TextField
          label='tagomi.script.edit.dialog.resourceName'
          required
          onChange={setResourceName}
          value={resourceName}
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
        <Button onClick={handleUpdate} disabled={updateDisabled}>
          {intl.formatMessage({ id: 'button.update' })}
        </Button>
      </DialogActions>
    </Dialog>
  );
};
