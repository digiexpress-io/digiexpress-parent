import React from 'react';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, generateUtilityClass, styled, Zoom } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';

import { TaskApi } from '@/api-task';
import { EveliTaskNotesEdit } from './EveliTaskNotesEdit';



export interface EditDialogProps {
  task: TaskApi.Task;
  open: boolean,
  onClose: () => void
}

export const EditNotesDialog: React.FC<EditDialogProps> = ({ task, open, onClose }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();

  return (
    <StyledEditNotesDialog className={classes.editDialog} open={open} onClose={onClose} maxWidth='md' slots={{ transition: Zoom }}>

      <DialogTitle>
        {intl.formatMessage({ id: 'task.notes.edit', defaultMessage: 'Edit notes for task' })}
        {intl.formatMessage({ id: 'eveli.textSeparatorColon' })}
        {task.taskRef ?? 'no task reference id'}
      </DialogTitle>

      <DialogContent sx={{p: 0}}>
        <EveliTaskNotesEdit task={task} />
      </DialogContent>

      <DialogActions>
        <Button variant='outlined' onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button onClick={onClose}>{intl.formatMessage({ id: 'button.save' })}</Button>
      </DialogActions>
    </StyledEditNotesDialog>
  )
}




const MUI_NAME = 'EditNotesDialog';
const StyledEditNotesDialog = styled(Dialog, {
  name: MUI_NAME,
  slot: 'EditDialog',
  overridesResolver: (_props, styles) => {
    return [
      styles.editDialog
    ];
  },

})(({ theme }) => {

  return {
     '.MuiDialogContent-root': {
      display: 'flex',
      flexDirection: 'column',
    }

  };
})


const useUtilityClasses = () => {
  const slots = {
    editDialog: ['editDialog'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
