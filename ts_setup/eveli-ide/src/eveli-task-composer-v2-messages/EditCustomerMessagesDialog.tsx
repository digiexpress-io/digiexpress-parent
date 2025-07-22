import { TaskApi } from '@/api-task';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, generateUtilityClass, styled, Zoom } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import React from 'react';
import { useIntl } from 'react-intl';
import { CustomerMessagesEdit } from './CustomerMessagesEdit';


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
        <CustomerMessagesEdit task={task} />

      </DialogContent>
      <DialogActions>
        <Button variant='outlined' onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button>{intl.formatMessage({ id: 'button.sendMessage', defaultMessage: 'Send message now' })}</Button>
      </DialogActions>
    </StyledDialog>
  )
}





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

  return {
    height: '100vh',
    '.MuiDialogContent-root': {
      display: 'flex',
      flexDirection: 'column',
      padding: 0,
      overflow: 'hidden'
    }
  };
})


const useUtilityClasses = () => {
  const slots = {
    editCustomerMessages: ['editCustomerMessages'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
