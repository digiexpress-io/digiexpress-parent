import React from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Divider, TextField, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';

export const TaskReopen: React.FC<{
  task: TaskApi.Task;
  open: boolean
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
        <DialogTitle>{intl.formatMessage({ id: 'taskReopenDialog.title' })}</DialogTitle>
        <DialogContent>
          <Typography>{intl.formatMessage({ id: 'taskReopenDialog.subtitle1' })}</Typography>
          <Typography>{intl.formatMessage({ id: 'taskReopenDialog.subtitle2' })}</Typography>

          <Divider sx={{ my: 3 }} />
          <Typography>{intl.formatMessage({ id: 'taskReopenDialog.customer-message' })}</Typography>

          <TextField
            required fullWidth multiline maxRows={5}
            value={reason}
            onChange={handleReasonChange}
            label={intl.formatMessage({ id: 'taskReopenDialog.customer-message-label-placeholder1' })}
            placeholder={intl.formatMessage({ id: 'taskReopenDialog.customer-message-label-placeholder2' })} />
        </DialogContent>
        <DialogActions>
          <Button variant='outlined' onClick={handleClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
          <Button variant='contained' disabled={disabled} onClick={handleSave}>{intl.formatMessage({ id: 'button.accept' })}</Button>
        </DialogActions>
      </Dialog>

      {backend.permissions.isReopenTaskAllowed && (<Button onClick={() => setOpen(true)}>
        {intl.formatMessage({ id: 'button.taskReopenDialog' })}
        </Button>)}

    </>
  );
}

