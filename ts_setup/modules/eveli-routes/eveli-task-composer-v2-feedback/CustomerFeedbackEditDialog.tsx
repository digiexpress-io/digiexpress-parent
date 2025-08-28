import React from 'react';
import { TaskApi } from '@dxs-ts/task-api';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, generateUtilityClass, styled, Zoom } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';

import { PublishedNotifier } from './PublishedNotifier';
import { UpsertOneFeedback } from '@dxs-ts/task-feedback';
import { useNavigate } from '@tanstack/react-router';


export interface CustomerFeedbackEditProps {
  task: TaskApi.Task,
  open: boolean,
  onClose: () => void
}

export const CustomerFeedbackEditDialog: React.FC<CustomerFeedbackEditProps> = ({ open, onClose, task }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const navigate = useNavigate();
  function onFeedbackCancel() {
    navigate({
      from: '/secured/$locale',
      params: { taskId: task.taskRef! },
      to: '/secured/$locale/worker/tasks/$taskId'
    });
  }
  return (
    <StyledDialog fullScreen className={classes.customerFeedbackEdit} open={open} onClose={onClose} slots={{ transition: Zoom }}>

      <DialogTitle sx={{ display: 'flex' }}>
        {intl.formatMessage({ id: 'task.customerFeedback', defaultMessage: 'Customer feedback' })}
        {intl.formatMessage({ id: 'eveli.textSeparatorColon' })}
        {task.taskRef ?? 'no task reference id'}
        <Box flexGrow={1} />
        <div>
          <PublishedNotifier task={task} />
        </div>
      </DialogTitle>

      <DialogContent>
        <UpsertOneFeedback 
          taskRef={task.taskRef!} 
          onComplete={() => { }} 
          onCancel={onFeedbackCancel}
          onDelete={onFeedbackCancel}
          reload={0} allowDelete={false} />
      </DialogContent>

      <DialogActions>
        <Button variant='outlined' onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button onClick={onClose}>{intl.formatMessage({ id: 'button.save' })}</Button>
      </DialogActions>
    </StyledDialog>
  )
}





const MUI_NAME = 'CustomerFeedbackEditDialog';
const StyledDialog = styled(Dialog, {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.customerFeedbackEdit
    ];
  },

})(({ theme }) => {

  return {
    height: '100vh',
    '.MuiDialogContent-root': {
      display: 'flex',
      flexDirection: 'column',
      // overflow: 'hidden'
    }
  };
})


const useUtilityClasses = () => {
  const slots = {
    customerFeedbackEdit: ['customerFeedbackEdit'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
