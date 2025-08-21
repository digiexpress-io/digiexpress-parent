import React from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Divider, generateUtilityClass, Grid2, SelectChangeEvent, styled, TextField, Typography, Zoom } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';

import { useTaskDashboard } from '../eveli-task-composer-v2/EveliTaskDashboardContext';
import { EditStatus } from './EditStatus';
import { EditPriority } from './EditPriority';
import { TaskApi } from '@dxs-ts/task-api';
import { EveliPermissions } from '@dxs-ts/eveli-primitives';


export interface PriorityStatusEditDialogProps {
  open: boolean;
  onClose: () => void
}


export const PriorityStatusEditDialog: React.FC<PriorityStatusEditDialogProps> = ({ open, onClose }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const { task, saveTask, isTaskChanged, saveCustomerComment } = useTaskDashboard();
  const [priority, setPriority] = React.useState<TaskApi.TaskPriority>(task.priority!);
  const [status, setStatus] = React.useState<TaskApi.TaskStatus>(task.status ?? TaskApi.TaskStatus.NEW);
  const [reopenReason, setReopenReason] = React.useState('');

  const handleStatusChange = async (e: SelectChangeEvent) => {
    const newStatus = e.target.value as TaskApi.TaskStatus;
    setStatus(newStatus);
  };

  const handlePriorityChange = async (level: TaskApi.TaskPriority) => {
    setPriority(level);
  };

  function handleReopenReasonChange(e: React.ChangeEvent<HTMLInputElement>) {
    setReopenReason(e.target.value)
  }

  function handleSaveReopenTask() {
    const newStatus = TaskApi.TaskStatus.OPEN;

    saveTask({ status: newStatus })
      .then(() => saveCustomerComment({ commentText: reopenReason }))
      .then(() => {
        setStatus(newStatus);
        setReopenReason('');
        onClose();
      })
      .catch((err) => {
        console.error('Failed to reopen task:', err);
      });
  }
  async function handleSave() {
    await saveTask({
      priority,
      status,
    });
    onClose();
  }

  const isTaskReopenable = (task.status === 'COMPLETED' || task.status === 'REJECTED') && task.questionnaireId;

  if (isTaskReopenable) {
    return (
      <EveliPermissions id='TASK_REOPEN'>
        <StyledPriorityStatusEditDialog className={classes.editDialog} open={open} onClose={onClose} maxWidth='md' slots={{ transition: Zoom }}>
          <DialogTitle>{intl.formatMessage({ id: 'taskReopenDialog.title', defaultMessage: 'Reopen Task' })}</DialogTitle>
          <DialogContent>
            <Typography>{intl.formatMessage({ id: 'taskReopenDialog.subtitle1', defaultMessage: 'You are about to reopen this task' })}</Typography>
            <Typography>{intl.formatMessage({ id: 'taskReopenDialog.subtitle2', defaultMessage: 'Upon accepting changes, task status will be set to OPEN and message will be sent to customer' })}</Typography>

            <Divider sx={{ my: 3 }} />
            <Typography>{intl.formatMessage({ id: 'taskReopenDialog.customer-message', defaultMessage: 'Please write a message to send to the customer explaining this change' })}</Typography>

            <StyledTextField required fullWidth multiline rows={5}
              value={reopenReason}
              onChange={handleReopenReasonChange}
              label={intl.formatMessage({ id: 'taskReopenDialog.customer-message-label-placeholder1', defaultMessage: 'Message to customer' })}
              placeholder={intl.formatMessage({ id: 'taskReopenDialog.customer-message-label-placeholder2', defaultMessage: 'Enter message' })} />
          </DialogContent>
          <DialogActions>
            <Button variant='outlined' onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
            <Button variant='contained' disabled={!reopenReason.trim()} onClick={handleSaveReopenTask}>{intl.formatMessage({ id: 'button.accept' })}</Button>
          </DialogActions>
        </StyledPriorityStatusEditDialog>
      </EveliPermissions>
    )
  }


  return (
    <StyledPriorityStatusEditDialog className={classes.editDialog} open={open} onClose={onClose} maxWidth='md' slots={{ transition: Zoom }}>
      <DialogTitle>
        {intl.formatMessage({ id: 'task.priorityAndStatusEdit', defaultMessage: 'Edit priority and status' })}
        {intl.formatMessage({ id: 'eveli.textSeparatorColon' })}
        {task.taskRef ?? 'no task reference id'}
      </DialogTitle>

      <DialogContent>
        <Grid2 container display='flex' alignItems='center'>
          <Grid2 size={{ md: 12, lg: 12, xl: 12 }}>
            <EditStatus onChange={handleStatusChange} status={status} />
          </Grid2>
          <Grid2 size={{ md: 12, lg: 12, xl: 12 }}>
            <EditPriority onChange={handlePriorityChange} priority={priority} />
          </Grid2>
        </Grid2>
      </DialogContent>
      <DialogActions>
        <Button variant='outlined' onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button onClick={handleSave} disabled={!isTaskChanged({ status, priority })}> {intl.formatMessage({ id: 'button.save' })}</Button>
      </DialogActions>
    </StyledPriorityStatusEditDialog>
  )
}


const MUI_NAME = 'PriorityStatusEditDialog';
const StyledPriorityStatusEditDialog = styled(Dialog, {
  name: MUI_NAME,
  slot: 'AssigneeRolesEdit',
  overridesResolver: (_props, styles) => {
    return [
      styles.editDialog
    ];
  },

})(({ theme }) => {

  return {};
})

const StyledTextField = styled(TextField)(({ theme }) => ({
  width: '100%',
  '& .MuiInputBase-input': {
    height: '2.5rem',
    padding: '0 12px'
  },
  '& .MuiInputBase-multiline': {
    paddingLeft: '0px',
    paddingRight: '0px'
  },
}));

const useUtilityClasses = () => {
  const slots = {
    editDialog: ['editDialog'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
