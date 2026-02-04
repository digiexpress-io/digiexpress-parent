import React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Box, Alert } from '@mui/material';
import { useSnackbar } from 'notistack';
import { useIntl } from 'react-intl';

import * as Burger from '@dxs-ts/eveli-primitives';
import { TagomiComposerApi as Composer, TagomiApi } from '@dxs-ts/tagomi-api';
import { CancelButton } from '@dxs-ts/eveli-primitives';


interface ImageEditProps {
  imageId: string;
  onClose: () => void;
}

export const ImageEdit: React.FC<ImageEditProps> = ({ imageId, onClose }) => {
  const { backend, actions, site } = Composer.useComposer();
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();

  const image = site.resources[imageId];
  const [resourceName, setResourceName] = React.useState(image.resourceName);
  const [uploadBody, setUploadBody] = React.useState<string | undefined>(undefined);
  const [previewUrl, setPreviewUrl] = React.useState<string | undefined>(undefined);
  const [uploadError, setUploadError] = React.useState<string>();
  const fileInputRef = React.useRef<HTMLInputElement>(null);

  const message = intl.formatMessage({ id: 'snack.image.editedMessage' }, { resourceName });

  async function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    try {
      const file: File = (e.target as any).files[0];
      if (!file) return;

      const reader = new FileReader();
      reader.onload = (event) => {
        const dataUrl = event.target?.result as string;
        const base64String = dataUrl.split(',')[1];
        setUploadBody(base64String);
        setPreviewUrl(dataUrl);
        setUploadError(undefined);
      };
      reader.onerror = () => {
        setUploadError('Failed to read file');
      };
      reader.readAsDataURL(file);
    } catch (error: any) {
      console.error(error);
      if (error instanceof Error) {
        setUploadError(error.message);
        return;
      }
      setUploadError(JSON.stringify(error));
    }
  }

  const handleUpdate = () => {
    setUploadError(undefined);
    const entity: TagomiApi.ResourceMutator = {
      resourceId: imageId,
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
    <Dialog open={true} onClose={onClose}>
      <input
        ref={fileInputRef}
        type="file"
        hidden
        accept="image/*"
        onChange={handleFileChange}
      />
      <DialogTitle>{intl.formatMessage({ id: 'tagomi.image.edit.dialog.title' })}{" "}{image.resourceName}</DialogTitle>

      <DialogContent>
        {!!uploadError && (
          <Alert severity="error">{uploadError}</Alert>
        )}

        <Burger.TextField
          label='tagomi.image.edit.dialog.resourceName'
          required
          onChange={setResourceName}
          value={resourceName}
        />

        <Box mb={2} />

        {(previewUrl || image.content) && (
          <Box mb={2} textAlign="center">
            <img
              src={previewUrl || `data:image/*;base64,${image.content}`}
              alt={resourceName}
              style={{ maxWidth: '100%', maxHeight: '300px', border: '1px solid #ccc', borderRadius: '4px' }}
            />
          </Box>
        )}

        <Button
          variant="outlined"
          onClick={() => fileInputRef.current?.click()}
          fullWidth
        >
          {uploadBody 
            ? intl.formatMessage({ id: 'tagomi.image.edit.dialog.fileSelected' }) 
            : intl.formatMessage({ id: 'tagomi.image.edit.dialog.uploadFile' })}
        </Button>
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
