import React from 'react';
import { CircularProgress, Stack } from '@mui/material';
import Burger from 'components-burger';

const DialobCreateActions: React.FC<{
  onClose: () => void,
  onCreate: () => void,
  disabled?: boolean,
  loading?: boolean
}> = ({ onClose, onCreate, disabled, loading }) => {

  function handleClose() {
    onClose();
  }

  async function handleCreateAndClose() {
    onCreate();
  }

  async function handleCreateAndEdit() {
    onCreate();
    // open editor
  }

  return (

    <Stack direction='row' spacing={1}>
      <Burger.SecondaryButton onClick={handleClose} label='dialob.form.create.dialog.button.cancel' />
      {loading ? <CircularProgress size='16pt' /> :
        <>
          <Burger.SecondaryButton onClick={handleCreateAndClose} label='dialob.form.create.dialog.button.createAndClose' disabled={disabled} />
          <Burger.PrimaryButton onClick={handleCreateAndEdit} label='dialob.form.create.dialog.button.createAndEdit' disabled={disabled} />
        </>
      }
    </Stack>

  );
}


export default DialobCreateActions;