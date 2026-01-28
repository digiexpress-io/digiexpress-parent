import React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Box, Alert } from '@mui/material';
import { useSnackbar } from 'notistack';
import { useIntl } from 'react-intl';

import * as Burger from '@dxs-ts/eveli-primitives';
import { TagomiComposerApi as Composer, TagomiApi } from '@dxs-ts/tagomi-api';
import { CancelButton } from '@dxs-ts/eveli-primitives';


export const ScriptComposer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { backend, actions } = Composer.useComposer();
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();
  const [resourceName, setResourceName] = React.useState("");
  const [uploadBody, setUploadBody] = React.useState("");
  const [uploadError, setUploadError] = React.useState<string>();
  const fileInputRef = React.useRef<HTMLInputElement>(null);

  const message = intl.formatMessage({ id: 'snack.script.createdMessage' }, { resourceName });

  async function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    try {
      const file: File = (e.target as any).files[0];
      if (!file) return;

      const arrayBuffer = await file.arrayBuffer();
      const base64String = btoa(
        new Uint8Array(arrayBuffer).reduce((data, byte) => data + String.fromCharCode(byte), '')
      );
      setUploadBody(base64String);
      setUploadError(undefined);

      if (!resourceName) {
        setResourceName(file.name);
      }
    } catch (error: any) {
      console.error(error);
      if (error instanceof Error) {
        setUploadError(error.message);
        return;
      }
      setUploadError(JSON.stringify(error));
    }
  }

  const handleCreate = () => {
    const entity: TagomiApi.CreateResource = {
      resourceName,
      contentType: 'SCRIPT',
      uploadBody,
      templateIds: []
    };

    backend.createResource(entity).then(success => {
      enqueueSnackbar(message, { variant: 'success' });
      onClose();
      actions.handleLoadSite();
    });
  };

  return (
    <Dialog open={true} onClose={onClose}>
      <input
        ref={fileInputRef}
        type="file"
        hidden
        onChange={handleFileChange}
      />
      <DialogTitle>{intl.formatMessage({ id: 'tagomi.script.create.dialog.title' })}</DialogTitle>

      <DialogContent>
        {!!uploadError && (
          <Alert severity="error">{uploadError}</Alert>
        )}

        <Burger.TextField
          label='tagomi.script.create.dialog.resourceName'
          onChange={setResourceName}
          value={resourceName ? resourceName : ''}
        />

        <Box mb={2} />

        <Button
          variant="outlined"
          onClick={() => fileInputRef.current?.click()}
          fullWidth
        >
          {uploadBody ? intl.formatMessage({ id: 'tagomi.script.create.dialog.fileSelected' }) : intl.formatMessage({ id: 'tagomi.script.create.dialog.uploadFile' })}
        </Button>
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
