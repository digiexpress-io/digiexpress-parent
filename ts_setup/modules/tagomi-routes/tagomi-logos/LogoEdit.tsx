import React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Box, Alert } from '@mui/material';
import { useSnackbar } from 'notistack';
import { useIntl } from 'react-intl';

import * as Burger from '@dxs-ts/eveli-primitives';
import { TagomiComposerApi as Composer, TagomiApi } from '@dxs-ts/tagomi-api';
import { CancelButton } from '@dxs-ts/eveli-primitives';


interface LogoEditProps {
  logoId: string;
  onClose: () => void;
}

export const LogoEdit: React.FC<LogoEditProps> = ({ logoId, onClose }) => {
  const { backend, actions, site } = Composer.useComposer();
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();

  const logo = site.resources[logoId];
  const [resourceName, setResourceName] = React.useState(logo.resourceName);
  const [uploadBody, setUploadBody] = React.useState<string | undefined>(undefined);
  const [uploadError, setUploadError] = React.useState<string>();
  const fileInputRef = React.useRef<HTMLInputElement>(null);

  const message = intl.formatMessage({ id: 'snack.logo.editedMessage' }, { resourceName });

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
    const entity: TagomiApi.ResourceMutator = {
      resourceId: logoId,
      resourceName,
      uploadBody
    };

    backend.updateResource(entity).then(_success => {
      enqueueSnackbar(message, { variant: 'success' });
      onClose();
      actions.handleLoadSite();
    });
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
      <DialogTitle>{intl.formatMessage({ id: 'tagomi.logo.edit.dialog.title' })}{" "}{logo.resourceName}</DialogTitle>

      <DialogContent>
        {!!uploadError && (
          <Alert severity="error">{uploadError}</Alert>
        )}

        <Burger.TextField
          label='tagomi.logo.edit.dialog.resourceName'
          required
          onChange={setResourceName}
          value={resourceName}
        />

        <Box mb={2} />

        <Button
          variant="outlined"
          onClick={() => fileInputRef.current?.click()}
          fullWidth
        >
          {uploadBody ? intl.formatMessage({ id: 'tagomi.logo.edit.dialog.fileSelected' }) : intl.formatMessage({ id: 'tagomi.logo.edit.dialog.uploadFile' })}
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
