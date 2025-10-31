import React from 'react';
import { useSnackbar } from 'notistack';

import { Typography, Dialog, DialogTitle, DialogContent, DialogActions, Button, FormHelperText } from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl';
import { StencilApi, StencilComposerApi as Composer } from '@dxs-ts/stencil-api';
import { CancelButton, EveliPermissions, TextField } from '@dxs-ts/eveli-primitives';



const ReleaseComposer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { enqueueSnackbar } = useSnackbar();
  const { service, actions, session } = Composer.useComposer();
  const { site } = session;
  const intl = useIntl();


  const [name, setName] = React.useState('');
  const [note, setNote] = React.useState('');
  const created = new Date().toISOString();

  const handleCreate = () => {
    const entity: StencilApi.CreateRelease = { name, note, created };
    service.create().release(entity).then(success => {
      enqueueSnackbar(message, { variant: 'success' });
      console.log(success)
      onClose();
      actions.handleLoadSite();
    })
  }
  const message = <FormattedMessage id="snack.release.createdMessage" />

  return (
    <Dialog open={true} onClose={onClose}>
      <DialogTitle><FormattedMessage id='release.composer.title' /></DialogTitle>
      <DialogContent>
        {site.contentType === 'EMPTY' ? <Typography variant="h4" sx={{
          color: 'error.main',
          fontWeight: 'bold',
          p: 2,
          borderRadius: 3,
          textAlign: 'center',
          border: '1px solid',
          borderColor: 'error.main'
        }}>
          <FormattedMessage id={'site.content.empty'} /></Typography> : null}
        <TextField label='release.composer.label' onChange={setName} value={name} />
        {!name && (
          <FormHelperText error>
            {intl.formatMessage({ id: 'error.valueRequired' })}
          </FormHelperText>
        )}
        <TextField label='release.composer.note' helperText='release.composer.helper' onChange={setNote} value={note} />
      </DialogContent>
      <DialogActions>
      <CancelButton onClick={onClose} />
        <EveliPermissions id='CREATE_STENCIL_ASSET'>
          <Button onClick={handleCreate} disabled={!name || site.contentType === 'EMPTY'}>
            <FormattedMessage id='button.create' />
          </Button>
        </EveliPermissions>
      </DialogActions>
    </Dialog>
  );
}

export { ReleaseComposer }