import React from 'react';
import { useSnackbar } from 'notistack';

import { Typography } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { Composer, StencilClient } from '../context';
import Burger from '@the-wrench-io/react-burger';



const ReleaseComposer: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const { enqueueSnackbar } = useSnackbar();
  const { service, actions, session } = Composer.useComposer();
  const { site } = session;


  const [name, setName] = React.useState('');
  const [note, setNote] = React.useState('');
  const created = new Date().toISOString();

  const handleCreate = () => {
    const entity: StencilClient.CreateRelease = { name, note, created };
    service.create().release(entity).then(success => {
      enqueueSnackbar(message, { variant: 'success' });
      console.log(success)
      onClose();
      actions.handleLoadSite();
    })
  }
  const message = <FormattedMessage id="snack.release.createdMessage" />

  return (
    <Burger.Dialog open={true} onClose={onClose}
      backgroundColor="uiElements.main"
      title="release.composer.title"
      submit={{ title: "button.create", onClick: handleCreate, disabled: !name || site.contentType === 'EMPTY' }}>
      <>
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
        <Burger.TextField label='release.composer.label' onChange={setName} value={name} />
        <Burger.TextField label='release.composer.note' helperText='release.composer.helper' onChange={setNote} value={note} />
      </>
    </Burger.Dialog>
  );
}

export { ReleaseComposer }