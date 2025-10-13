import React from 'react';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, generateUtilityClass, styled, Typography, Zoom } from '@mui/material';
import { History as HistoryIcon } from '@mui/icons-material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';


import { CustomerMessagesEditor } from './CustomerMessagesEditor';
import { useTaskDashboard } from '../task-dashboard';


export interface CustomerMessagesEditDialogProps {
  open: boolean,
  onClose: () => void
}


export const CustomerMessagesEditDialog: React.FC<CustomerMessagesEditDialogProps> = ({ open, onClose }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const [newMessage, setNewMessage] = React.useState<string>('');
  const { task, saveCustomerComment } = useTaskDashboard();


  function handleSetMessage(event: React.ChangeEvent<HTMLInputElement>) {
    setNewMessage(event.target.value);
  }

  async function handleSendMessage() {
    await saveCustomerComment({ commentText: newMessage });
    setNewMessage('')
  }

  function handleCloseDialog() {
    onClose();
  }


  return (
    <StyledDialog fullWidth maxWidth='xl' className={classes.customerMessagesEdit} open={open} onClose={onClose} slots={{ transition: Zoom }}>

      <DialogTitle>
        {intl.formatMessage({ id: 'task.customerMessages' })}
        {intl.formatMessage({ id: 'eveli.textSeparatorColon' })}
        {task.taskRef ?? 'no task reference id'}
      </DialogTitle>

      <DialogContent>
        <Box className={classes.historyLabel}>
          <HistoryIcon />
          <Typography>{intl.formatMessage({ id: 'task.customerMessages.messageHistory', defaultMessage: 'Message history' })}</Typography>
        </Box>
        <CustomerMessagesEditor onChange={handleSetMessage} task={task} message={newMessage} />
      </DialogContent>

      <DialogActions>
        <Button variant='outlined' onClick={handleCloseDialog}>{intl.formatMessage({ id: 'button.close' })}</Button>
        <Button onClick={handleSendMessage} disabled={!newMessage.trim()}>{intl.formatMessage({ id: 'button.sendMessage', defaultMessage: 'Send message now' })}</Button>
      </DialogActions>
    </StyledDialog>
  )
}





const MUI_NAME = 'CustomerMessagesEditDialog';
const StyledDialog = styled(Dialog, {
  name: MUI_NAME,
  slot: 'Messages',
  overridesResolver: (_props, styles) => {
    return [
      styles.customerMessagesEdit
    ];
  },

})(({ theme }) => {

  return {
    height: '100vh',
    '.CustomerMessagesEditDialog-historyLabel': {
      marginLeft: theme.spacing(1),
      display: 'flex',
      alignItems: 'center',
      '& .MuiTypography-root': {
        ...theme.typography.body2,
        fontWeight: 'bold'
      },
      '& .MuiSvgIcon-root': {
        fontSize: '20pt',
        marginRight: theme.spacing(1),
        marginLeft: theme.spacing(1),
        color: theme.palette.primary.main
      },
    },
    '.MuiDialogContent-root': {
      display: 'flex',
      flexDirection: 'column',
      overflow: 'hidden'
    },


  };
})


const useUtilityClasses = () => {
  const slots = {
    customerMessagesEdit: ['customerMessagesEdit'],
    historyLabel: ['historyLabel']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
