import React from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Divider, TextField, Typography } from '@mui/material';
import { useIntl } from 'react-intl';

import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';



export const TaskReopen: React.FC<{
  task: TaskApi.Task;
  onReload: () => Promise<void>;
}> = (props) => {
  const intl = useIntl();
  const [open, setOpen] = React.useState(false);
  const [reason, setReason] = React.useState('');
  const backend = useTaskBackend();

  function handleClose() {
    setReason('');
    setOpen(false);
  }

  async function handleSave() {
    const updatedTask = await backend.persistence.modifyOneTask({ ...props.task, status: TaskApi.TaskStatus.OPEN });
    await backend.persistence.createOneComment(reason, undefined, updatedTask, true);
    props.onReload().then(handleClose);
  }

  function handleReasonChange(e: React.ChangeEvent<HTMLInputElement>) {
    setReason(e.target.value)
  }

  const disabled = !reason.trim();
  const isTaskReopenable = (props.task.status === 'COMPLETED' || props.task.status === 'REJECTED') && props.task.questionnaireId;

  if (!isTaskReopenable) {
    return (<></>)
  }

  return (
    <>
      <Dialog open={open} onClose={handleClose}>
        <DialogTitle>{intl.formatMessage({ id: 'taskReopenDialog.title', defaultMessage: 'Reopen Task' })}</DialogTitle>
        <DialogContent>
          <Typography>{intl.formatMessage({ id: 'taskReopenDialog.subtitle1', defaultMessage: 'You are about to reopen this task' })}</Typography>
          <Typography>{intl.formatMessage({ id: 'taskReopenDialog.subtitle2', defaultMessage: 'Upon accepting changes, task status will be set to OPEN and message will be sent to customer' })}</Typography>

          <Divider sx={{ my: 3 }} />
          <Typography>{intl.formatMessage({ id: 'taskReopenDialog.customer-message', defaultMessage: 'Please write a message to send to the customer explaining this change' })}</Typography>

          <TextField
            required fullWidth multiline maxRows={5}
            value={reason}
            onChange={handleReasonChange}
            label={intl.formatMessage({ id: 'taskReopenDialog.customer-message-label-placeholder1', defaultMessage: 'Message to customer' })}
            placeholder={intl.formatMessage({ id: 'taskReopenDialog.customer-message-label-placeholder2', defaultMessage: 'Enter message' })} />
        </DialogContent>
        <DialogActions>
          <Button variant='outlined' onClick={handleClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
          <Button variant='contained' disabled={disabled} onClick={handleSave}>{intl.formatMessage({ id: 'button.accept' })}</Button>
        </DialogActions>
      </Dialog>

      {
       backend.permissions.isReopenTaskAllowed && (
        <Button onClick={() => setOpen(true)}>
          {intl.formatMessage({ id: 'button.taskReopenDialog', defaultMessage: 'Reopen task' })}
        </Button>)
      }

    </>
  );
}

