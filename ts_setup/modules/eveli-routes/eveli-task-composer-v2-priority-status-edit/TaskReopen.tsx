import React from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Divider, TextField, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { TaskApi } from '@dxs-ts/eveli-api';
import { useFetch } from '@dxs-ts/envir-fetch';
import { EveliPermissions } from '@dxs-ts/eveli-primitives';


export const TaskReopen: React.FC<{
  task: TaskApi.Task;
  open: boolean
}> = (props) => {
  const intl = useIntl();
  const [open, setOpen] = React.useState(false);
  const [reason, setReason] = React.useState('');
  const { updateTask } = useFetch('worker/rest/api/tasks/$taskId.PUT', {});
  const { saveComment } = useFetch('worker/rest/api/tasks/$taskId/comments.POST', {});


  function handleClose() {
    setReason('');
    setOpen(false);
  }

  async function handleSave() {
    const updatedTask = await updateTask({ ...props.task, status: TaskApi.TaskStatus.OPEN });
    await saveComment(reason, undefined, updatedTask, true);
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
      <Dialog open={props.open} onClose={handleClose}>
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

      <EveliPermissions id='TASK_REOPEN'>
        <Button onClick={() => setOpen(true)}>{intl.formatMessage({ id: 'button.taskReopenDialog', defaultMessage: 'Reopen task' })}</Button>
      </EveliPermissions>

    </>
  );
}

