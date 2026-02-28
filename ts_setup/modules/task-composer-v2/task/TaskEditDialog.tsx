import React from 'react';
import { Button, DialogActions, DialogContent, DialogTitle, Grid2, Typography, Zoom } from '@mui/material';
import { useIntl } from 'react-intl';

import { TaskProperties } from './TaskProperties';
import { useTaskDashboard } from '../task-dashboard';
import { DatePicker } from '@dxs-ts/xui-datetime';
import { useUtilityClasses, StyledTaskEditDialog, StyledTextField, StyledDatePicker } from './useUtilityClasses';

export interface TaskEditDialogProps {
  open: boolean,
  onClose: () => void
}

function useSubjectErrors(subject: string | undefined): undefined | string {
  const intl = useIntl();
  if (!subject) {
    return intl.formatMessage({ id: 'error.valueRequired' })
  }
  if (subject.length < 3) {
    return intl.formatMessage({ id: 'error.minTextLength' }, { minLength: 3 })
  }
}

export const TaskEditDialog: React.FC<TaskEditDialogProps> = ({ open, onClose }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const { task, saveTask, isTaskChanged } = useTaskDashboard();

  const initialDueDate: Date | null =
    task.dueDate instanceof Date
      ? task.dueDate
      : (task.dueDate ? new Date(task.dueDate as any) : null);

  const [dueDate, setDueDate] = React.useState<Date | null>(initialDueDate);
  const [addInfo, setAddInfo] = React.useState(task.additionalInfo);
  const [subject, setSubject] = React.useState(task.subject);
  const [dueDateError, setDueDateError] = React.useState<string | undefined>();

  const subjectErrors = useSubjectErrors(subject);
  const isErrors = !!subjectErrors || !!dueDateError;

  function handleSetAddInfo(event: React.ChangeEvent<HTMLInputElement>) {
    setAddInfo(event.target.value);
  }
  function handleSetSubject(event: React.ChangeEvent<HTMLInputElement>) {
    setSubject(event.target.value);
  }
  async function handleSave() {
    await saveTask({
      dueDate: dueDate ?? undefined,
      subject,
      additionalInfo: addInfo,
    });
    onClose();
  }

  return (
    <StyledTaskEditDialog
      className={classes.editDialog}
      open={open}
      onClose={onClose}
      maxWidth='md'
      slots={{ transition: Zoom }}
    >
      <DialogTitle>
        {intl.formatMessage({ id: 'task.edit' })} {task.taskRef ?? intl.formatMessage({ id: 'task.reference.missing' })}
      </DialogTitle>

      <DialogContent>
        <Grid2 container display='flex' alignItems='center'>
          <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
            <Typography fontWeight='bold'>
              {intl.formatMessage({ id: 'task.dueDate' })}
            </Typography>
          </Grid2>
          <Grid2 size={{ md: 9, lg: 9, xl: 9 }}>
            <StyledDatePicker
              fullWidth
              size="small"
              value={dueDate}
              onChange={(date) => setDueDate(date)}
              onValidity={(isError) =>
                setDueDateError(isError ? intl.formatMessage({ id: 'taskcard.body.dueDate.value.invalid' }) : undefined)
              }
            />
            {dueDateError && (
              <Typography variant="caption" color="error" ml={2}>
                {dueDateError}
              </Typography>
            )}
          </Grid2>

          <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
            <Typography fontWeight='bold'>{intl.formatMessage({ id: 'task.customerName' })}</Typography>
          </Grid2>
          <Grid2 size={{ md: 9, lg: 9, xl: 9 }}>
            <StyledTextField value={task.clientIdentificator} disabled />
          </Grid2>

          <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
            <Typography fontWeight='bold'>{intl.formatMessage({ id: 'task.subject' })}</Typography>
          </Grid2>
          <Grid2 size={{ md: 9, lg: 9, xl: 9 }}>
            <StyledTextField value={subject} onChange={handleSetSubject} error={!!subjectErrors} helperText={subjectErrors || ''} />
          </Grid2>

          <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
            <Typography fontWeight='bold'>{intl.formatMessage({ id: 'task.additionalInfo' })}</Typography>
          </Grid2>
          <Grid2 size={{ md: 9, lg: 9, xl: 9 }}>
            <StyledTextField rows={3} multiline value={addInfo} onChange={handleSetAddInfo} />
          </Grid2>

          <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
            <Typography fontWeight='bold'>{intl.formatMessage({ id: 'task.metaData' })}</Typography>
          </Grid2>
          <Grid2 size={{ md: 9, lg: 9, xl: 9 }}>
            <TaskProperties task={task} />
          </Grid2>
        </Grid2>
      </DialogContent>
      <DialogActions>
        <Button variant='outlined' onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button
          onClick={handleSave}
          disabled={
            !isTaskChanged({
              additionalInfo: addInfo,
              dueDate: dueDate ?? undefined,
              subject,
            }) || isErrors
          }>
          {intl.formatMessage({ id: 'button.save' })}
        </Button>
      </DialogActions>
    </StyledTaskEditDialog>
  )
}