import React from 'react';
import { Stack } from '@mui/material';
import Burger from 'components-burger';


const TaskCreateActions: React.FC<{ onClose: () => void, disabled?: boolean }> = ({ onClose, disabled }) => {

  function handleClose() {
    onClose();
  }

  async function handleCreateAndClose() {
    handleClose();
  }

  async function handleCreateAndEdit() {
    handleClose();
  }

  return (

    <Stack direction='row' spacing={1}>
      <Burger.SecondaryButton onClick={handleClose} label='dialob.form.create.dialog.button.cancel' />
      <Burger.SecondaryButton onClick={handleCreateAndClose} label='dialob.form.create.dialog.button.createAndClose' disabled={disabled} />
      <Burger.PrimaryButton onClick={handleCreateAndEdit} label='dialob.form.create.dialog.button.createAndEdit' disabled={disabled} />
    </Stack>

  );
}


export default TaskCreateActions;