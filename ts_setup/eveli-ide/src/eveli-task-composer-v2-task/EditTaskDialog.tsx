import React from 'react';
import { TaskApi } from '@/api-task';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, generateUtilityClass, Grid2, styled, TextField, Typography, Zoom } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';

import { TaskProperties } from './TaskProperties';


export interface EditDialogProps {
  task: TaskApi.Task;
  open: boolean,
  onClose: () => void
}

export const EditTaskDialog: React.FC<EditDialogProps> = ({ task, open, onClose }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();


  return (
    <StyledEditTaskDialog className={classes.editDialog} open={open} onClose={onClose} maxWidth='md' slots={{ transition: Zoom }}>

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
            <StyledTextField value={task.dueDate?.toISOString()} />
          </Grid2>
          

          <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
            <Typography fontWeight='bold'>{intl.formatMessage({ id: 'task.customerName' })}</Typography>
          </Grid2>
          <Grid2 size={{ md: 9, lg: 9, xl: 9 }}>
            <StyledTextField value={task.clientIdentificator} />
          </Grid2>

          <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
            <Typography fontWeight='bold'>{intl.formatMessage({ id: 'task.subject' })}</Typography>
          </Grid2>
          <Grid2 size={{ md: 9, lg: 9, xl: 9 }}>
            <StyledTextField value={task.subject} />
          </Grid2>

          <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
            <Typography fontWeight='bold'>{intl.formatMessage({ id: 'task.additionalInfo' })}</Typography>
          </Grid2>
          <Grid2 size={{ md: 9, lg: 9, xl: 9 }}>
            <StyledTextField rows={3} multiline value={task.additionalInfo} />
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
        <Button onClick={onClose}>{intl.formatMessage({ id: 'button.save' })}</Button>
      </DialogActions>
    </StyledEditTaskDialog>
  )
}

const StyledTextField = styled(TextField)(({ theme }) => ({
  width: '100%',
  '& .MuiInputBase-input': {
    height: '2.5rem',
    padding: '0 12px'
  },
}));



const MUI_NAME = 'EditTaskDialog';
const StyledEditTaskDialog = styled(Dialog, {
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
