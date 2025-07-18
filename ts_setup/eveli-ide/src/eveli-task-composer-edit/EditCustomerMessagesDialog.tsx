import { TaskApi } from '@/api-task';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, generateUtilityClass, Grid2, styled, TextField, Typography, Zoom } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import React from 'react';
import { useIntl } from 'react-intl';


export interface EditCustomerMessagesProps {
  task: TaskApi.Task;
  open: boolean,
  onClose: () => void
}

export const EditCustomerMessagesDialog: React.FC<EditCustomerMessagesProps> = ({ task, open, onClose }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();


  return (
    <StyledDialog className={classes.editCustomerMessages} open={open} onClose={onClose} fullWidth maxWidth='lg' slots={{ transition: Zoom }}>

      <DialogTitle>
        {intl.formatMessage({ id: 'task.customerMessages' })}
        {intl.formatMessage({ id: 'eveli.textSeparatorColon' })}
        {task.taskRef ?? 'no task reference id'}
      </DialogTitle>

      <DialogContent>
        <Grid2 container display='flex' alignItems='center'>
        <Box display='flex' flexDirection='column'>{task.comments.map(c => <Box>{c.commentText}</Box>)}</Box>
        </Grid2>

      </DialogContent>
      <DialogActions>
        <Button variant='outlined' onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button onClick={onClose}>{intl.formatMessage({ id: 'button.save' })}</Button>
      </DialogActions>
    </StyledDialog>
  )
}

const StyledTextField = styled(TextField)(({ theme }) => ({
  width: '100%',
  '& .MuiInputBase-input': {
    height: '2.5rem',
    padding: '0 12px'
  },
}));



const MUI_NAME = 'EditCustomerMessagesDialog';
const StyledDialog = styled(Dialog, {
  name: MUI_NAME,
  slot: 'Messages',
  overridesResolver: (_props, styles) => {
    return [
      styles.EditCustomerMessages
    ];
  },

})(({ theme }) => {

  return {};
})


const useUtilityClasses = () => {
  const slots = {
    editCustomerMessages: ['editCustomerMessages'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
