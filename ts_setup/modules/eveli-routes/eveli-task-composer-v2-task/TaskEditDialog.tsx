import React from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, generateUtilityClass, Grid2, styled, TextField, Typography, Zoom } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';

import { TaskProperties } from './TaskProperties';
import { useTaskDashboard } from '../eveli-task-composer-v2/EveliTaskDashboardContext';


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
  const [dueDate, setDueDate] = React.useState(task.dueDate); 
  const [addInfo, setAddInfo] = React.useState(task.additionalInfo);
  const [subject, setSubject] = React.useState(task.subject);
  const subjectErrors = useSubjectErrors(subject)

  //TODO
  function handleDueDate() {

  }

  function handleSetAddInfo(event: React.ChangeEvent<HTMLInputElement>) {
    setAddInfo(event.target.value);
  }
  function handleSetSubject(event: React.ChangeEvent<HTMLInputElement>) {
    setSubject(event.target.value);
  }
  async function handleSave() {
    await saveTask({ dueDate, subject, additionalInfo: addInfo });
    onClose();
  }

  return (
    <StyledTaskEditDialog className={classes.editDialog} open={open} onClose={onClose} maxWidth='md' slots={{ transition: Zoom }}>
      <DialogTitle>
        {intl.formatMessage({ id: 'task.edit' })}
        {intl.formatMessage({ id: 'eveli.textSeparatorColon' })}
        {task.taskRef ?? 'no task reference id'}
      </DialogTitle>

      <DialogContent>
        <Grid2 container display='flex' alignItems='center'>

          <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
            <Typography fontWeight='bold'>{intl.formatMessage({ id: 'task.dueDate' })}</Typography>
          </Grid2>
          <Grid2 size={{ md: 7, lg: 7, xl: 7 }}>
            <StyledTextField value={dueDate} onChange={() => { }} />
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
            <TaskProperties task={task}/>
          </Grid2>
        </Grid2>
      </DialogContent>
      <DialogActions>
        <Button variant='outlined' onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button onClick={handleSave}
          disabled={!isTaskChanged({ additionalInfo: addInfo, dueDate, subject }) || !!subjectErrors}>
          {intl.formatMessage({ id: 'button.save' })}
        </Button>
      </DialogActions>
    </StyledTaskEditDialog>
  )
}

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



const MUI_NAME = 'TaskEditDialog';
const StyledTaskEditDialog = styled(Dialog, {
  name: MUI_NAME,
  slot: 'EditDialog',
  overridesResolver: (_props, styles) => {
    return [
      styles.editDialog
    ];
  },

})(({ theme }) => {

  return {};
})


const useUtilityClasses = () => {
  const slots = {
    editDialog: ['editDialog'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
