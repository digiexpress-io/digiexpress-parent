import React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Box, Alert } from '@mui/material';
import { useSnackbar } from 'notistack';
import { useIntl } from 'react-intl';

import * as Burger from '@dxs-ts/eveli-primitives';
import { TagomiComposerApi as Composer, TagomiApi } from '@dxs-ts/tagomi-api';
import { CancelButton } from '@dxs-ts/eveli-primitives';


export const ImageComposer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { backend, actions } = Composer.useComposer();
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();
  const [resourceName, setResourceName] = React.useState("");
  const [uploadBody, setUploadBody] = React.useState<string>();
  const [uploadError, setUploadError] = React.useState<string>();
  const fileInputRef = React.useRef<HTMLInputElement>(null);

  const message = intl.formatMessage({ id: 'snack.image.createdMessage' }, { resourceName });

  async function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    try {
      const file: File = (e.target as any).files[0];
      if (!file) return;

      const reader = new FileReader();
      reader.onload = (event) => {
        const dataUrl = event.target?.result as string;
        const base64String = dataUrl.split(',')[1];
        setUploadBody(base64String);
        setUploadError(undefined);
      };
      reader.onerror = () => {
        setUploadError('Failed to read file');
      };
      reader.readAsDataURL(file);

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
    if (!uploadBody) {
      setUploadError('Please select an image file');
      return;
    }

    setUploadError(undefined);
    const entity: TagomiApi.CreateResource = {
      resourceName,
      contentType: 'image/*',
      uploadBody,
      templateIds: []
    };

    backend.createResource(entity)
      .then(_success => {
        enqueueSnackbar(message, { variant: 'success' });
        onClose();
        actions.handleLoadSite();
      })
  };

  return (
    <Dialog open={true} onClose={onClose}>
      <input
        ref={fileInputRef}
        type="file"
        hidden
        accept="image/*"
        onChange={handleFileChange}
      />
      <DialogTitle>{intl.formatMessage({ id: 'tagomi.image.create.dialog.title' })}</DialogTitle>

      <DialogContent>
        {!!uploadError && (
          <Alert severity="error">{uploadError}</Alert>
        )}

        <Burger.TextField
          label='tagomi.image.create.dialog.resourceName'
          onChange={setResourceName}
          value={resourceName ? resourceName : ''}
        />

        <Box mb={2} />

        <Button
          variant="outlined"
          onClick={() => fileInputRef.current?.click()}
          fullWidth
        >
          {uploadBody ? intl.formatMessage({ id: 'tagomi.image.create.dialog.fileSelected' }) : intl.formatMessage({ id: 'tagomi.image.create.dialog.uploadFile' })}
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
